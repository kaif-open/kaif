package io.kaif.service.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountDao;
import io.kaif.model.account.Authority;
import io.kaif.model.account.Authorization;
import io.kaif.model.clientapp.ClientApp;
import io.kaif.model.clientapp.ClientAppDao;
import io.kaif.model.clientapp.ClientAppScope;
import io.kaif.model.clientapp.ClientAppUser;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
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

  private static final Duration GRANT_CODE_DURATION = Duration.ofMinutes(10);

  //we are just follow github, grant long term access token instead of requiring refresh periodically
  private static final Duration ACCESS_TOKEN_EXPIRE_DURATION = Duration.ofDays(20 * 365);

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
    return clientAppDao.createApp(account, name, description, callbackUri, Instant.now());
  }

  private Account verifyDeveloper(Authorization creator) {
    return accountDao.strongVerifyAccount(creator)
        .filter(a -> a.containsAuthority(Authority.CITIZEN))
        .orElseThrow(() -> new AccessDeniedException("no authority on client app."));
  }

  @Override
  public ClientApp loadClientAppWithoutCache(String clientId) {
    return clientAppDao.loadAppWithoutCache(clientId);
  }

  @Override
  public List<ClientApp> listClientApps(Authorization creator) {
    return clientAppDao.listAppOrderByTime(creator.authenticatedId());
  }

  @Override
  public void update(Authorization creator,
      String clientId,
      String name,
      String description,
      String callbackUri) {
    ClientApp clientApp = verifyClientAppForOwner(creator, clientId);
    clientAppDao.updateAppInformation(clientApp.withName(name)
        .withDescription(description)
        .withCallbackUri(callbackUri));
  }

  @Override
  public Optional<ClientApp> verifyRedirectUri(String clientId, String redirectUri) {
    return clientAppDao.findApp(clientId).filter(app -> app.validateRedirectUri(redirectUri));
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
          .map(grantCode -> createOauthAccessToken(clientApp,
              grantCode.getAccountId(),
              grantCode.getScopes()));
    }).orElseThrow(() -> new AccessDeniedException("invalid grant for oauth access token"));
  }

  /**
   * back door to create ClientAppUser and accessToken directly, for ease of testing
   */
  @VisibleForTesting
  OauthAccessTokenDto createOauthAccessToken(ClientApp clientApp,
      UUID accountId,
      Set<ClientAppScope> scopes) {
    Account account = accountDao.findById(accountId).get();
    clientAppDao.mergeClientAppUser(account, clientApp, scopes, Instant.now());
    ClientAppUserAccessToken accessToken = new ClientAppUserAccessToken(account.getAccountId(),
        account.getAuthorities(),
        scopes,
        clientApp.getClientId(),
        clientApp.getClientSecret());
    String encodedToken = accessToken.encode(Instant.now().plus(ACCESS_TOKEN_EXPIRE_DURATION),
        oauthSecret);
    return new OauthAccessTokenDto(encodedToken,
        accessToken.getCanonicalScope(),
        Oauths.DEFAULT_TOKEN_TYPE);
  }

  /**
   * unlike AccountAccessToken, which have two variations (one is in memory verify, the other is
   * hit db for strong verify). ClientAppAccessToken are always check against db, but with one
   * minute local caching window. So in worst cases, user or app revoke, the token require wait up
   * to one minute to take effect when kaif deploy to multiple servers.
   */
  @Override
  public Optional<ClientAppUserAccessToken> verifyAccessToken(@Nullable String rawAccessToken) {
    return ClientAppUserAccessToken.tryDecode(rawAccessToken, oauthSecret).filter(token -> {
      Optional<ClientAppUser> clientAppUser = clientAppDao.findClientAppUserWithCache(token.authenticatedId(),
          token.clientId());
      return token.validate(clientAppUser.orElse(null));
    });
  }

  @Override
  public List<ClientAppUser> listGrantedApps(Authorization authorization) {
    return clientAppDao.listAppsByUser(authorization.authenticatedId());
  }

  @Override
  public void resetClientAppSecret(Authorization creator, String clientId) {
    ClientApp clientApp = verifyClientAppForOwner(creator, clientId);
    clientAppDao.updateAppSecret(clientApp.withResetSecret());
  }

  private ClientApp verifyClientAppForOwner(Authorization creator, String clientId) {
    Account account = verifyDeveloper(creator);
    ClientApp clientApp = clientAppDao.loadAppWithoutCache(clientId);
    if (!account.belongToAccount(clientApp.getOwnerAccountId())) {
      throw new AccessDeniedException("not client app owner");
    }
    return clientApp;
  }

  @Override
  public void revokeApp(Authorization user, String clientId) {
    clientAppDao.deleteClientAppUser(user.authenticatedId(), clientId);
  }

  @Override
  public boolean validateApp(String clientId, String clientSecret) {
    return clientAppDao.findApp(clientId)
        .filter(app -> app.getClientSecret().equals(clientSecret))
        .isPresent();
  }
}
