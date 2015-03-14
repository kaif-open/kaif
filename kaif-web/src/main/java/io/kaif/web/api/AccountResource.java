package io.kaif.web.api;

import java.util.Locale;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.account.AccountAuth;
import io.kaif.model.exception.AuthenticateFailException;
import io.kaif.service.AccountService;
import io.kaif.service.FeedService;
import io.kaif.web.support.SingleWrapper;

@RestController
@RequestMapping("/api/account")
public class AccountResource {

  static class AccountRequest {

    @Size(min = Account.NAME_MIN, max = Account.NAME_MAX)
    @NotNull
    @Pattern(regexp = Account.NAME_PATTERN)
    public String username;

    @Size(min = Account.PASSWORD_MIN, max = Account.PASSWORD_MAX)
    @NotNull
    public String password;

    @Email
    @NotNull
    public String email;

  }

  static class Credential {
    @NotNull
    public String username;
    @NotNull
    public String password;
  }

  static class SendResetPasswordRequest {
    @NotNull
    public String username;

    @Email
    @NotNull
    public String email;
  }

  static class UpdatePasswordWithTokenRequest {

    @Size(min = Account.PASSWORD_MIN, max = Account.PASSWORD_MAX)
    @NotNull
    public String password;

    @NotNull
    public String token;

  }

  static class UpdateNewPasswordRequest {

    @Size(min = Account.PASSWORD_MIN, max = Account.PASSWORD_MAX)
    @NotNull
    public String oldPassword;

    @Size(min = Account.PASSWORD_MIN, max = Account.PASSWORD_MAX)
    @NotNull
    public String newPassword;

  }

  static class UpdateDescriptionRequest {
    public String description;
  }

  static class PreviewDescription {
    public String description;
  }

  static class NewsFeedAcknowledge {
    @NotNull
    public FlakeId assetId;
  }

  @Autowired
  private AccountService accountService;
  @Autowired
  private FeedService feedService;

  @RequestMapping(value = "/", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void create(@Valid @RequestBody AccountRequest request, Locale locale) {
    accountService.createViaEmail(request.username.trim(),
        request.email.trim(),
        request.password,
        locale);
  }

  /**
   * force json for better CSRF protection
   */
  @RequestMapping(value = "/authenticate", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public AccountAuth authenticate(@Valid @RequestBody Credential credential) {
    return accountService.authenticate(credential.username.trim(), credential.password)
        .orElseThrow(AuthenticateFailException::new);
  }

  @RequestMapping(value = "/extends-access-token", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public AccountAuth extendsAccessToken(AccountAccessToken token) {
    return accountService.extendsAccessToken(token);
  }

  @RequestMapping(value = "/resend-activation", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void resendActivation(AccountAccessToken token, Locale locale) {
    accountService.resendActivation(token, locale);
  }

  @RequestMapping(value = "/email-available")
  public SingleWrapper<Boolean> isEmailAvailable(@RequestParam("email") String email) {
    return SingleWrapper.of(accountService.isEmailAvailable(email));
  }

  @RequestMapping(value = "/name-available")
  public SingleWrapper<Boolean> isNameAvailable(@RequestParam("username") String username) {
    return SingleWrapper.of(accountService.isUsernameAvailable(username));
  }

  @RequestMapping(value = "/news-feed-unread")
  public SingleWrapper<Integer> newsFeedUnreadCount(AccountAccessToken token) {
    return SingleWrapper.of(feedService.countUnread(token));
  }

  @RequestMapping(value = "/news-feed-acknowledge", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void newsFeedAcknowledge(AccountAccessToken token,
      @Valid @RequestBody NewsFeedAcknowledge ack) {
    feedService.acknowledge(token, ack.assetId);
  }

  @RequestMapping(value = "/send-reset-password", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void sendResetPassword(@Valid @RequestBody SendResetPasswordRequest request,
      Locale locale) {
    accountService.sendResetPassword(request.username, request.email, locale);
  }

  @RequestMapping(value = "/update-password-with-token", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void updatePasswordWithOnceToken(
      @Valid @RequestBody UpdatePasswordWithTokenRequest request, Locale locale) {
    accountService.updatePasswordWithOnceToken(request.token, request.password, locale);
  }

  @RequestMapping(value = "/update-new-password", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public AccountAuth updateNewPassword(AccountAccessToken accessToken,
      @Valid @RequestBody UpdateNewPasswordRequest request,
      Locale locale) {
    return accountService.updateNewPassword(accessToken,
        request.oldPassword,
        request.newPassword,
        locale);
  }

  @RequestMapping(value = "/description", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public String updateDescription(AccountAccessToken accessToken,
      @Valid @RequestBody UpdateDescriptionRequest request) {
    return accountService.updateDescription(accessToken, request.description);
  }

  //this method is public, do not need authenticate
  @RequestMapping(value = "/description/preview", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public String previewDescriptionContent(@Valid @RequestBody PreviewDescription request) {
    return Account.renderDescriptionPreview(request.description);
  }

  @RequestMapping(value = "/description", method = RequestMethod.GET)
  public String loadEditableDescription(AccountAccessToken token) {
    return accountService.loadEditableDescription(token);
  }

}
