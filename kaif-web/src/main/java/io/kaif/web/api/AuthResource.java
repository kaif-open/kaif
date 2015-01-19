package io.kaif.web.api;

import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.kaif.model.AccountService;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.account.AccountAuth;
import io.kaif.web.support.RestAccessDeniedException;

@RestController
@RequestMapping("/api/auth")
public class AuthResource {

  @Autowired
  private AccountService accountService;

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
  public AccountAuth authenticate(@Valid Credential credential) {
    Optional<AccountAuth> accountAuth = accountService.authenticate(credential.name,
        credential.password);
    return accountAuth.orElseThrow(RestAccessDeniedException::new);
  }

  @RequestMapping(value = "/extends-token", method = RequestMethod.POST)
  public AccountAuth extendsToken(AccountAccessToken token) {
    return accountService.extendsAccessToken(token);
  }
}
