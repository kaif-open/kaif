package io.kaif.service;

import java.util.List;
import java.util.Optional;

import io.kaif.model.account.Authorization;
import io.kaif.model.clientapp.ClientApp;
import io.kaif.web.support.AccessDeniedException;

public interface ClientAppService {

  ClientApp create(Authorization authorization,
      String name,
      String description,
      String callbackUri);

  ClientApp loadClientAppWithoutCache(String clientId);

  List<ClientApp> listClientApps(Authorization creator);

  void update(Authorization creator,
      String clientId,
      String name,
      String description,
      String callbackUri);

  Optional<ClientApp> verifyRedirectUri(String clientId, String redirectUri);

  String directGrantCode(String oauthDirectAuthorize,
      String clientId,
      String scope,
      String redirectUri) throws AccessDeniedException;

  Optional<String> createOauthAccessTokenByGrantCode(String code,
      String clientId,
      String redirectUri);
}
