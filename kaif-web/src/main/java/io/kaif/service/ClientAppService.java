package io.kaif.service;

import io.kaif.model.account.Authorization;
import io.kaif.model.clientapp.ClientApp;

public interface ClientAppService {
  ClientApp create(Authorization authorization,
      String name,
      String description,
      String callbackUri);

  ClientApp loadClientAppWithoutCache(String clientId);
}
