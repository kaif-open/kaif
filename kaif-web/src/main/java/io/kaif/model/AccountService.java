package io.kaif.model;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.account.AccountAuth;
import io.kaif.model.account.AccountDao;
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

  public Account createViaEmail(String name, String email, String password) {
    Preconditions.checkArgument(Account.isValidPassword(password));
    Preconditions.checkArgument(name != null && name.length() >= 3);
    Preconditions.checkNotNull(email);
    //TODO duplicate name exception
    //TODO duplicate email exception
    return accountDao.create(name, email, passwordEncoder.encode(password));
  }

  @VisibleForTesting
  Account findById(String accountId) {
    return accountDao.findById(UUID.fromString(accountId));
  }

  public Optional<AccountAuth> authenticate(String name, String password) {
    return accountDao.findByName(name)
        .filter(account -> passwordEncoder.matches(password, account.getPasswordHash()))
        .map(this::createAccountAuth);
    //TODO not activate treat as fail
  }

  //TODO activate via email
  private AccountAuth createAccountAuth(Account account) {
    Instant expireTime = Instant.now().plus(ACCOUNT_TOKEN_EXPIRE);
    String accessToken = new AccountAccessToken(account.getPasswordHash(),
        account.getName(),
        account.getAuthorities()).encode(expireTime, accountSecret);
    return new AccountAuth(account.getAccountId(),
        account.getName(),
        accessToken,
        account.getAuthorities(),
        expireTime);
  }

  /**
   * the verification go against database, so it is slow. using {@link
   * io.kaif.model.account.AccountAccessToken#tryDecode(String, io.kaif.model.account.AccountSecret)}
   * if you want faster check.
   */
  public boolean verifyAccessToken(String rawAccessToken) {
    return verifyAccessTokenWithDao(rawAccessToken).isPresent();
  }

  private Optional<Account> verifyAccessTokenWithDao(String rawAccessToken) {
    return AccountAccessToken.tryDecode(rawAccessToken, accountSecret)
        .flatMap(token -> accountDao.findByName(token.getName()).filter(account -> {
          // verify database already change password or authorities
          return account.getPasswordHash().equals(token.getPasswordHash())
              && account.getAuthorities().equals(token.getAuthorities());
        }));
  }

  public Optional<AccountAuth> extendsAccessToken(String accessToken) {
    return verifyAccessTokenWithDao(accessToken).map(this::createAccountAuth);
  }

  public boolean isNameAvailable(String name) {
    return !accountDao.findByName(name).isPresent();
  }

  public void updateAuthorities(String accountId, EnumSet<Authority> authorities) {
    accountDao.updateAuthorities(UUID.fromString(accountId), authorities);
  }

  public void updatePassword(String accountId, String password) {
    Preconditions.checkArgument(Account.isValidPassword(password));
    accountDao.updatePasswordHash(UUID.fromString(accountId), passwordEncoder.encode(password));
  }
}
