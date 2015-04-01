package io.kaif.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.account.AccountAuth;
import io.kaif.model.account.AccountOnceToken;
import io.kaif.model.account.AccountStats;
import io.kaif.model.account.Authority;
import io.kaif.model.account.Authorization;
import io.kaif.model.exception.OldPasswordNotMatchException;

public interface AccountService {

  Account createViaEmail(String username, String email, String password, Locale locale);

  Optional<Account> findMe(Authorization authorization);

  Optional<AccountAuth> authenticate(String username, String password);

  Optional<AccountAccessToken> strongVerifyAccessToken(String rawAccessToken);

  AccountAuth extendsAccessToken(AccountAccessToken accountAccessToken);

  boolean isUsernameAvailable(String username);

  boolean isEmailAvailable(String email);

  void updateAuthorities(Authorization authorization, EnumSet<Authority> authorities);

  boolean activate(String inputOnceToken);

  void resendActivation(Authorization authorization, Locale locale);

  void sendResetPassword(String username, String email, Locale locale);

  Optional<AccountOnceToken> findValidResetPasswordToken(String inputOnceToken);

  void updatePasswordWithOnceToken(String inputOnceToken, String password, Locale locale);

  AccountAuth updateNewPassword(Authorization authorization,
      String oldPassword,
      String newPassword,
      Locale locale) throws OldPasswordNotMatchException;

  Optional<AccountAccessToken> tryDecodeAccessToken(String rawAccountAccessToken);

  AccountStats loadAccountStats(String username);

  Account loadAccount(String caseInsensitiveUsername);

  String updateDescription(Authorization authorization, String description);

  String loadEditableDescription(Authorization authorization);

  void complaintEmail(List<String> emails);

  void muteEmail(List<String> emails);

  AccountOnceToken createOauthDirectAuthorizeToken(Authorization authorization);

  boolean oauthDirectAuthorize(String inputOnceToken);
}
