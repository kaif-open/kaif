package io.kaif.service;

import java.util.List;

import io.kaif.model.account.Authorization;
import io.kaif.model.clientapp.ClientApp;

public interface ClientAppService {
  ClientApp create(Authorization authorization,
      String name,
      String description,
      String callbackUri);

  ClientApp loadClientAppWithoutCache(String clientId);

  List<ClientApp> listClientApps(Authorization creator);
}
