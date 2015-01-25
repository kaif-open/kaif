package io.kaif.model;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

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
import io.kaif.model.account.Authority;

@Service
@Transactional
public class AccountService {

  private static final Duration ACCOUNT_TOKEN_EXPIRE = Duration.ofDays(30);
  @Autowired
  private AccountDao accountDao;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private AccountSecret accountSecret;

  @Autowired
  private MailAgent mailAgent;

  private Clock clock = Clock.systemDefaultZone();

  public Account createViaEmail(String name, String email, String password, Locale locale) {
    Preconditions.checkArgument(Account.isValidPassword(password));
    Preconditions.checkArgument(Account.isValidName(name));
    Preconditions.checkNotNull(email);
    Instant now = Instant.now(clock);
    Account account = accountDao.create(name, email, passwordEncoder.encode(password), now);
    AccountOnceToken token = accountDao.createOnceToken(account,
        AccountOnceToken.Type.ACTIVATION,
        now);

    //async send email, no wait
    mailAgent.sendAccountActivation(locale, account, token.getToken());
    return account;
  }

  public Account findById(UUID accountId) {
    return accountDao.findById(accountId).orElse(null);
  }

  public Optional<AccountAuth> authenticate(String name, String password) {
    return accountDao.findByName(name)
        .filter(account -> passwordEncoder.matches(password, account.getPasswordHash()))
        .map(this::createAccountAuth);
  }

  private AccountAuth createAccountAuth(Account account) {
    Instant expireTime = Instant.now(clock).plus(ACCOUNT_TOKEN_EXPIRE);
    String accessToken = new AccountAccessToken(account.getAccountId(),
        account.getPasswordHash(),
        account.getAuthorities()).encode(expireTime, accountSecret);
    return new AccountAuth(account.getName(), accessToken, expireTime.toEpochMilli());
  }

  /**
   * the verification go against database, so it is slow. using {@link
   * io.kaif.model.account.AccountAccessToken#tryDecode(String, io.kaif.model.account.AccountSecret)}
   * if you want faster check.
   */
  public Optional<AccountAccessToken> verifyAccessToken(String rawAccessToken) {
    return AccountAccessToken.tryDecode(rawAccessToken, accountSecret)
        .filter(token -> verifyTokenToAccount(token).isPresent());
  }

  private Optional<Account> verifyTokenToAccount(AccountAccessToken token) {
    return accountDao.findById(token.getAccountId()).filter(account -> {
      // verify database already change password or authorities
      return token.matches(account.getPasswordHash(), account.getAuthorities());
    });
  }

  public AccountAuth extendsAccessToken(AccountAccessToken accessToken) {
    return Optional.ofNullable(accessToken)
        .flatMap(token -> accountDao.findById(token.getAccountId()))
        .map(this::createAccountAuth)
        .get();
  }

  public boolean isNameAvailable(String name) {
    return !accountDao.findByName(name).isPresent();
  }

  public boolean isEmailAvailable(String email) {
    return accountDao.isEmailAvailable(email);
  }

  public void updateAuthorities(UUID accountId, EnumSet<Authority> authorities) {
    accountDao.updateAuthorities(accountId, authorities);
  }

  public void updatePassword(UUID accountId, String password) {
    Preconditions.checkArgument(Account.isValidPassword(password));
    accountDao.updatePasswordHash(accountId, passwordEncoder.encode(password));
  }

  public boolean activate(String token) {
    return accountDao.findOnceToken(token, AccountOnceToken.Type.ACTIVATION)
        .filter(onceToken -> !onceToken.isComplete())
        .filter(onceToken -> !onceToken.isExpired(Instant.now(clock)))
        .map(onceToken -> {
          Account account = accountDao.findById(onceToken.getAccountId()).get();
          EnumSet<Authority> newAuth = EnumSet.copyOf(account.getAuthorities());
          newAuth.add(Authority.CITIZEN);
          accountDao.updateAuthorities(account.getAccountId(), newAuth);
          accountDao.completeOnceToken(onceToken);
          return true;
        })
        .orElse(false);
  }

  @VisibleForTesting
  void setClock(Clock clock) {
    this.clock = clock;
  }
}
