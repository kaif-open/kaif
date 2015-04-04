package io.kaif.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountDao;
import io.kaif.model.account.Authority;
import io.kaif.model.account.Authorization;
import io.kaif.model.clientapp.ClientApp;
import io.kaif.model.clientapp.ClientAppDao;
import io.kaif.model.clientapp.ClientAppScope;
import io.kaif.model.clientapp.GrantCode;
import io.kaif.model.clientapp.OauthSecret;
import io.kaif.model.exception.ClientAppMaxException;
import io.kaif.oauth.OauthAccessTokenDto;
import io.kaif.oauth.Oauths;
import io.kaif.service.AccountService;
import io.kaif.service.ClientAppService;
import io.kaif.web.support.AccessDeniedException;

@Service
@Transactional
public class ClientAppServiceImpl implements ClientAppService {

  public static final Duration GRANT_CODE_DURATION = Duration.ofHours(1);
  @Autowired
  private AccountDao accountDao;

  @Autowired
  private ClientAppDao clientAppDao;
  @Autowired
  private AccountService accountService;
  @Autowired
  private OauthSecret oauthSecret;

  @Override
  public ClientApp create(Authorization creator,
      String name,
      String description,
      String callbackUri) throws ClientAppMaxException {
    Account account = verifyDeveloper(creator);
    if (listClientApps(account).size() >= ClientApp.MAX_NO_OF_APPS) {
      throw new ClientAppMaxException(ClientApp.MAX_NO_OF_APPS);
    }
    return clientAppDao.create(account, name, description, callbackUri, Instant.now());
  }

  private Account verifyDeveloper(Authorization creator) {
    return accountDao.strongVerifyAccount(creator)
        .filter(a -> a.containsAuthority(Authority.CITIZEN))
        .orElseThrow(() -> new AccessDeniedException("no authority on client app."));
  }

  @Override
  public ClientApp loadClientAppWithoutCache(String clientId) {
    return clientAppDao.loadWithoutCache(clientId);
  }

  @Override
  public List<ClientApp> listClientApps(Authorization creator) {
    return clientAppDao.listOrderByTime(creator.authenticatedId());
  }

  @Override
  public void update(Authorization creator,
      String clientId,
      String name,
      String description,
      String callbackUri) {
    Account account = verifyDeveloper(creator);
    ClientApp clientApp = clientAppDao.loadWithoutCache(clientId);
    if (!account.belongToAccount(clientApp.getOwnerAccountId())) {
      throw new AccessDeniedException("not client app owner");
    }
    clientAppDao.update(clientApp.withName(name)
        .withDescription(description)
        .withCallbackUri(callbackUri));
  }

  @Override
  public Optional<ClientApp> verifyRedirectUri(String clientId, String redirectUri) {
    return clientAppDao.find(clientId).filter(app -> app.validateRedirectUri(redirectUri));
  }

  @Override
  public String directGrantCode(String oauthDirectAuthorize,
      String clientId,
      String scope,
      String redirectUri) throws AccessDeniedException {

    //before grant code step, redirect uri, client id, scope all should be verified

    ClientApp clientApp = verifyRedirectUri(clientId,
        redirectUri).orElseThrow(() -> new IllegalStateException("invalid clientId and redirectUri"));

    Set<ClientAppScope> clientAppScopes = ClientAppScope.tryParse(scope);
    if (clientAppScopes.isEmpty()) {
      throw new IllegalStateException("invalid scope");
    }

    Account account = accountService.oauthDirectAuthorize(oauthDirectAuthorize)
        .orElseThrow(() -> new AccessDeniedException("direct authorize failed"));

    return new GrantCode(account.getAccountId(),
        clientApp.getClientId(),
        clientApp.getClientSecret(),
        redirectUri,
        clientAppScopes).encode(Instant.now().plus(GRANT_CODE_DURATION), oauthSecret);
  }

  /**
   * if failed to create access token, mostly cause by grant code validation failed
   * the server should response error=invalid_grant
   */
  @Override
  public OauthAccessTokenDto createOauthAccessTokenByGrantCode(String code,
      String clientId,
      String redirectUri) throws AccessDeniedException {
    return verifyRedirectUri(clientId, redirectUri).flatMap(clientApp -> {
      return GrantCode.tryDecode(code, oauthSecret)
          .filter(grantCode -> grantCode.matches(clientApp, redirectUri))
          .map(validCode -> {
            //TODO generate oauthAccessToken
            return new OauthAccessTokenDto(UUID.randomUUID().toString(),
                validCode.getCanonicalScope(),
                Oauths.DEFAULT_TOKEN_TYPE);
          });
    }).orElseThrow(() -> new AccessDeniedException("invalid grant for oauth access token"));
  }
}
