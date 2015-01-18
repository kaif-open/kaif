package io.kaif.model;

import java.time.Duration;
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
    Preconditions.checkArgument(password != null && password.length() >= 6);
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

  private AccountAuth createAccountAuth(Account account) {
    String accessToken = new AccountAccessToken(account.getPasswordHash(),
        account.getName(),
        account.getAuthorities()).encode(ACCOUNT_TOKEN_EXPIRE, accountSecret);
    return new AccountAuth(account.getAccountId(),
        account.getName(),
        accessToken,
        account.getAuthorities());
  }

  //TODO activate via email

  public boolean verifyAccessToken(String rawAccessToken) {
    return verifyAccessTokenWithDao(rawAccessToken).isPresent();
  }

  //TODO every request verify to db is too slow
  private Optional<Account> verifyAccessTokenWithDao(String rawAccessToken) {
    return AccountAccessToken.tryDecode(rawAccessToken, accountSecret).flatMap(token -> {
      //TODO verify passwordHash seems wrong (should use encoder match?)
      return accountDao.findByName(token.getName())
          .filter(account -> account.getPasswordHash().equals(token.getPasswordHash()));
    });
  }

  public Optional<AccountAuth> extendsAccessToken(String accessToken) {
    return verifyAccessTokenWithDao(accessToken).map(this::createAccountAuth);
  }

  public boolean isNameAvailable(String name) {
    return !accountDao.findByName(name).isPresent();
  }
}
