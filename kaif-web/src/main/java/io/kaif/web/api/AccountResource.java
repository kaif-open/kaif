package io.kaif.web.api;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.kaif.model.AccountService;

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
  public void create(@Valid AccountRequest request) {
    accountService.createViaEmail(request.name, request.email, request.password);
  }
}
