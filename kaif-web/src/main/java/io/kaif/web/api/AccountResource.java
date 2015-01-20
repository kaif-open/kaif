package io.kaif.web.api;

import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.kaif.model.AccountService;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.account.AccountAuth;
import io.kaif.web.support.RestAccessDeniedException;

@RestController
@RequestMapping("/api/account")
public class AccountResource {

  static class AccountRequest {
    @NotNull
    public String name;
    @NotNull
    public String password;
    @Email
    public String email;
  }

  @Autowired
  private AccountService accountService;

  @RequestMapping(value = "/", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void create(@Valid @RequestBody AccountRequest request) {
    accountService.createViaEmail(request.name, request.email, request.password);
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
    Optional<AccountAuth> accountAuth = accountService.authenticate(credential.name,
        credential.password);
    return accountAuth.orElseThrow(RestAccessDeniedException::new);
  }

  @RequestMapping(value = "/extends-access-token", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public AccountAuth extendsAccessToken(AccountAccessToken token) {
    return accountService.extendsAccessToken(token);
  }
}
