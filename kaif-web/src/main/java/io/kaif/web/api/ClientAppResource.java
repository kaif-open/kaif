package io.kaif.web.api;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.clientapp.ClientApp;
import io.kaif.service.ClientAppService;
import io.kaif.web.support.SingleWrapper;

@RestController
@RequestMapping("/api/client-app")
public class ClientAppResource {

  static class CreateClientApp {

    @Size(min = ClientApp.NAME_MIN, max = ClientApp.NAME_MAX)
    @NotNull
    public String name;

    @Size(min = ClientApp.DESCRIPTION_MIN, max = ClientApp.DESCRIPTION_MAX)
    @NotNull
    public String description;

    @URL
    @NotNull
    public String callbackUri;

  }

  @Autowired
  private ClientAppService clientAppService;

  @RequestMapping(value = "/create", method = RequestMethod.PUT)
  public SingleWrapper<String> create(AccountAccessToken accountAccessToken,
      @RequestBody @Valid CreateClientApp create) {
    String clientId = clientAppService.create(accountAccessToken,
        create.name.trim(),
        create.description.trim(),
        create.callbackUri.trim()).getClientId();
    return SingleWrapper.of(clientId);
  }

}
