package io.kaif.service;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.account.AccountAuth;
import io.kaif.model.account.AccountOnceToken;
import io.kaif.model.account.AccountStats;
import io.kaif.model.account.Authority;
import io.kaif.model.exception.OldPasswordNotMatchException;

public interface AccountService {

  Account createViaEmail(String username, String email, String password, Locale locale);

  Optional<Account> findById(UUID accountId);

  Optional<AccountAuth> authenticate(String username, String password);

  Optional<AccountAccessToken> verifyAccessToken(String rawAccessToken);

  AccountAuth extendsAccessToken(AccountAccessToken accessToken);

  boolean isUsernameAvailable(String username);

  boolean isEmailAvailable(String email);

  void updateAuthorities(UUID accountId, EnumSet<Authority> authorities);

  boolean activate(String token);

  void resendActivation(UUID accountId, Locale locale);

  void sendResetPassword(String username, String email, Locale locale);

  Optional<AccountOnceToken> findValidResetPasswordToken(String token);

  void updatePasswordWithToken(String token, String password, Locale locale);

  AccountAuth updateNewPassword(UUID accountId,
      String oldPassword,
      String newPassword,
      Locale locale) throws OldPasswordNotMatchException;

  Optional<AccountAccessToken> tryDecodeAccessToken(String token);

  AccountStats loadAccountStats(UUID accountId);
}
