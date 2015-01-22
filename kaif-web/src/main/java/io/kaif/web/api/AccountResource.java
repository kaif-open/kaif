package io.kaif.web.api;

import java.util.Optional;

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

import io.kaif.model.AccountService;
import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.account.AccountAuth;
import io.kaif.web.support.RestAccessDeniedException;
import io.kaif.web.support.SingleWrapper;

@RestController
@RequestMapping("/api/account")
public class AccountResource {

  static class AccountRequest {

    @Size(min = Account.NAME_MIN, max = Account.NAME_MAX)
    @NotNull
    @Pattern(regexp = Account.NAME_PATTERN)
    public String name;

    @Size(min = Account.PASSWORD_MIN, max = Account.PASSWORD_MAX)
    @NotNull
    public String password;

    @Email
    @NotNull
    public String email;

  }

  @Autowired
  private AccountService accountService;

  @RequestMapping(value = "/", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void create(@Valid @RequestBody AccountRequest request) {
    accountService.createViaEmail(request.name.trim(), request.email.trim(), request.password);
  }

  static class Credential {
    @NotNull
    public String name;
    @NotNull
    public String password;
  }

  /**
   * force json for better CSRF protection
   */
  @RequestMapping(value = "/authenticate", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public AccountAuth authenticate(@Valid @RequestBody Credential credential) {
    Optional<AccountAuth> accountAuth = accountService.authenticate(credential.name.trim(),
        credential.password);
    return accountAuth.orElseThrow(RestAccessDeniedException::new);
  }

  @RequestMapping(value = "/extends-access-token", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public AccountAuth extendsAccessToken(AccountAccessToken token) {
    return accountService.extendsAccessToken(token);
  }

  @RequestMapping(value = "/email-available")
  public SingleWrapper<Boolean> isEmailAvailable(@RequestParam("email") String email) {
    return SingleWrapper.of(accountService.isEmailAvailable(email));
  }

  @RequestMapping(value = "/name-available")
  public SingleWrapper<Boolean> isNameAvailable(@RequestParam("name") String name) {
    return SingleWrapper.of(accountService.isNameAvailable(name));
  }
}
