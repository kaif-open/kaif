package io.kaif.web.api;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

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

    /**
     * do not use `@URL` to validate because it does not allow custom scheme other than http
     */
    @Pattern(regexp = ClientApp.CALLBACK_URI_PATTERN)
    @NotNull
    public String callbackUri;

  }

  static class UpdateClientApp {

    @NotNull
    public String clientId;

    @Size(min = ClientApp.NAME_MIN, max = ClientApp.NAME_MAX)
    @NotNull
    public String name;

    @Size(min = ClientApp.DESCRIPTION_MIN, max = ClientApp.DESCRIPTION_MAX)
    @NotNull
    public String description;

    /**
     * do not use `@URL` to validate because it does not allow custom scheme other than http
     */
    @Pattern(regexp = ClientApp.CALLBACK_URI_PATTERN)
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

  @RequestMapping(value = "/update", method = RequestMethod.POST)
  public void update(AccountAccessToken accountAccessToken,
      @RequestBody @Valid UpdateClientApp update) {
    clientAppService.update(accountAccessToken,
        update.clientId,
        update.name.trim(),
        update.description.trim(),
        update.callbackUri.trim());
  }
}
