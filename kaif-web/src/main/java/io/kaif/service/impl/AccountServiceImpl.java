package io.kaif.service.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import io.kaif.mail.MailAgent;
import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.account.AccountAuth;
import io.kaif.model.account.AccountDao;
import io.kaif.model.account.AccountOnceToken;
import io.kaif.model.account.AccountSecret;
import io.kaif.model.account.AccountStats;
import io.kaif.model.account.Authority;
import io.kaif.model.account.Authorization;
import io.kaif.model.exception.OldPasswordNotMatchException;
import io.kaif.service.AccountService;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

  private static final Duration ACCOUNT_TOKEN_EXPIRE = Duration.ofDays(30);

  private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

  @Autowired
  private AccountDao accountDao;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private AccountSecret accountSecret;

  @Autowired
  private MailAgent mailAgent;

  private Clock clock = Clock.systemDefaultZone();

  @Override
  public Account createViaEmail(String username, String email, String password, Locale locale) {
    Preconditions.checkArgument(Account.isValidPassword(password));
    Preconditions.checkArgument(Account.isValidUsername(username));
    Preconditions.checkNotNull(email);
    Instant now = Instant.now(clock);
    Account account = accountDao.create(username, email, passwordEncoder.encode(password), now);
    sendOnceAccountActivation(account, locale, now);
    return account;
  }

  private void sendOnceAccountActivation(Account account, Locale locale, Instant now) {
    AccountOnceToken token = accountDao.createOnceToken(account,
        AccountOnceToken.Type.ACTIVATION,
        now);

    //async send email, no wait
    mailAgent.sendAccountActivation(locale, account, token.getToken());
  }

  @Override
  public Optional<Account> findMe(Authorization authorization) {
    return accountDao.findById(authorization.authenticatedId());
  }

  @Override
  public Optional<AccountAuth> authenticate(String username, String password) {
    return accountDao.findByUsername(username)
        .filter(account -> passwordEncoder.matches(password, account.getPasswordHash()))
        .map(this::createAccountAuth);
  }

  private AccountAuth createAccountAuth(Account account) {
    Instant now = Instant.now(clock);
    Instant expireTime = now.plus(ACCOUNT_TOKEN_EXPIRE);
    String accessToken = new AccountAccessToken(account.getAccountId(),
        account.getPasswordHash(),
        account.getAuthorities()).encode(expireTime, accountSecret);
    return new AccountAuth(account.getUsername(),
        accessToken,
        expireTime.toEpochMilli(),
        now.toEpochMilli());
  }

  /**
   * the verification go against database, so it is slow. using {@link
   * #tryDecodeAccessToken(String)} if you want faster check.
   */
  @Override
  public Optional<AccountAccessToken> strongVerifyAccessToken(String rawAccessToken) {
    return AccountAccessToken.tryDecode(rawAccessToken, accountSecret)
        .filter(auth -> accountDao.strongVerifyAccount(auth).isPresent());
  }

  @Override
  public AccountAuth extendsAccessToken(AccountAccessToken accessToken) {
    return Optional.ofNullable(accessToken)
        .flatMap(accountDao::strongVerifyAccount)
        .map(this::createAccountAuth)
        .get();
  }

  @Override
  public boolean isUsernameAvailable(String username) {
    return !accountDao.findByUsername(username).isPresent();
  }

  @Override
  public boolean isEmailAvailable(String email) {
    return accountDao.isEmailAvailable(email);
  }

  @Override
  public void updateAuthorities(Authorization authorization, EnumSet<Authority> authorities) {
    accountDao.strongVerifyAccount(authorization)
        .ifPresent(account -> accountDao.updateAuthorities(account, authorities));
  }

  private void updatePassword(UUID accountId, String password, Locale locale) {
    Preconditions.checkArgument(Account.isValidPassword(password));
    accountDao.updatePasswordHash(accountId, passwordEncoder.encode(password));
    accountDao.findById(accountId)
        .ifPresent(account -> mailAgent.sendPasswordWasReset(locale, account));
  }

  @Override
  public boolean activate(String inputOnceToken) {
    return accountDao.findOnceToken(inputOnceToken, AccountOnceToken.Type.ACTIVATION)
        .filter(token -> token.isValid(Instant.now(clock)))
        .map(onceToken -> {
          Account account = accountDao.findById(onceToken.getAccountId()).get();
          EnumSet<Authority> newAuth = EnumSet.copyOf(account.getAuthorities());
          newAuth.add(Authority.CITIZEN);
          accountDao.updateAuthorities(account, newAuth);
          accountDao.completeOnceToken(onceToken);
          return true;
        })
        .orElse(false);
  }

  @VisibleForTesting
  void setClock(Clock clock) {
    this.clock = clock;
  }

  @Override
  public void resendActivation(Authorization authorization, Locale locale) {
    accountDao.strongVerifyAccount(authorization)
        .ifPresent(account -> sendOnceAccountActivation(account, locale, Instant.now(clock)));
  }

  @Override
  public void sendResetPassword(String username, String email, Locale locale) {
    logger.info("begin reset password: {}, {}", username, email);
    accountDao.findByUsername(username)
        .filter(account -> account.getEmail().equals(email))
        .ifPresent(account -> {
          AccountOnceToken token = accountDao.createOnceToken(account,
              AccountOnceToken.Type.FORGET_PASSWORD,
              Instant.now(clock));
          mailAgent.sendResetPassword(locale, account, token.getToken());
        });
  }

  @Override
  public Optional<AccountOnceToken> findValidResetPasswordToken(String inputOnceToken) {
    return accountDao.findOnceToken(inputOnceToken, AccountOnceToken.Type.FORGET_PASSWORD)
        .filter(token -> token.isValid(Instant.now(clock)));
  }

  @Override
  public void updatePasswordWithOnceToken(String accountOnceToken, String password, Locale locale) {
    findValidResetPasswordToken(accountOnceToken).ifPresent(onceToken -> {
      accountDao.completeOnceToken(onceToken);
      updatePassword(onceToken.getAccountId(), password, locale);
    });
  }

  @Override
  public AccountAuth updateNewPassword(Authorization authorization,
      String oldPassword,
      String newPassword,
      Locale locale) throws OldPasswordNotMatchException {
    return accountDao.strongVerifyAccount(authorization)
        .filter(account -> passwordEncoder.matches(oldPassword, account.getPasswordHash()))
        .flatMap(accountWithOldPassword -> {
          updatePassword(authorization.authenticatedId(), newPassword, locale);
          //reload with new password
          return accountDao.findById(authorization.authenticatedId());
        })
        .map(this::createAccountAuth)
        .orElseThrow(OldPasswordNotMatchException::new);
  }

  @Override
  public Optional<AccountAccessToken> tryDecodeAccessToken(String rawAccountAccessToken) {
    return AccountAccessToken.tryDecode(rawAccountAccessToken, accountSecret);
  }

  @Override
  public AccountStats loadAccountStats(String username) {
    return accountDao.loadStats(username);
  }

  @Override
  public Account loadAccount(String username) {
    return accountDao.loadByUsername(username);
  }

  @Override
  public String updateDescription(Authorization authorization, String description) {
    return accountDao.strongVerifyAccount(authorization).flatMap(account -> {
      accountDao.updateDescription(account.getAccountId(), description);
      return accountDao.findById(authorization.authenticatedId());
    }).map(Account::getRenderDescription).orElse("");
  }

  @Override
  public String loadEditableDescription(Authorization authorization) {
    return accountDao.strongVerifyAccount(authorization)
        .map(Account::getEscapedDescription)
        .orElse("");
  }

  @Override
  public void complaintEmail(List<String> emails) {
    //TODO handle AWS email complaint
  }

  @Override
  public void muteEmail(List<String> emails) {
    //TODO handle AWS permanent Bounced Emails
  }
}
