package io.kaif.service.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountOnceToken;
import io.kaif.model.clientapp.ClientApp;
import io.kaif.model.clientapp.ClientAppScope;
import io.kaif.model.clientapp.ClientAppUser;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.model.exception.CallbackUriReservedException;
import io.kaif.model.exception.ClientAppMaxException;
import io.kaif.model.exception.ClientAppNameReservedException;
import io.kaif.oauth.OauthAccessTokenDto;
import io.kaif.service.AccountService;
import io.kaif.test.DbIntegrationTests;
import io.kaif.web.support.AccessDeniedException;

public class ClientAppServiceImplTest extends DbIntegrationTests {

  @Autowired
  private ClientAppServiceImpl service;
  @Autowired
  private AccountService accountService;
  private Account dev;

  @Before
  public void setUp() throws Exception {
    dev = savedAccountCitizen("dev1");
  }

  @Test
  public void create() throws Exception {
    ClientApp clientApp = service.create(dev, "myapp", "ya ~ good", "http://myapp.com/callback");
    ClientApp loaded = service.loadClientAppWithoutCache(clientApp.getClientId());
    assertEquals("myapp", loaded.getAppName());
    assertEquals("ya ~ good", loaded.getDescription());
    assertEquals("http://myapp.com/callback", loaded.getCallbackUri());
    assertEquals(32, loaded.getClientSecret().length());
    assertEquals(16, loaded.getClientId().length());
    assertEquals(dev.getAccountId(), loaded.getOwnerAccountId());
    assertNotNull(loaded.getCreateTime());
  }

  @Test
  public void verifyRedirectUri() throws Exception {
    assertFalse(service.verifyRedirectUri("notExist", "foo://com").isPresent());
    ClientApp clientApp = service.create(dev, "myapp", "ya ~ good", "http://myapp.com/callback");
    String id = clientApp.getClientId();
    assertEquals(clientApp, service.verifyRedirectUri(id, "http://myapp.com/callback").get());
    assertTrue(service.verifyRedirectUri(id, "http://myapp.com/callback/").isPresent());
    assertTrue(service.verifyRedirectUri(id, "http://myapp.com/callback/bar").isPresent());
    assertFalse(service.verifyRedirectUri(id, "http://myapp.com/wrong").isPresent());
    assertFalse(service.verifyRedirectUri(id, "https://myapp.com/callback/foo").isPresent());
    assertFalse(service.verifyRedirectUri(id, "myapp://callback/foo").isPresent());
    assertFalse(service.verifyRedirectUri(id, null).isPresent());
  }

  @Test
  public void create_maxApps() throws Exception {
    IntStream.rangeClosed(1, 5).forEach(i -> {
      service.create(dev, "myapp", "ya ~ good", "http://myapp.com/callback");
    });
    try {
      service.create(dev, "myapp", "ya ~ good", "http://myapp.com/callback");
      fail("ClientAppMaxException expected");
    } catch (ClientAppMaxException expected) {
    }
  }

  @Test
  public void update() throws Exception {
    ClientApp clientApp = service.create(dev, "myapp", "ya ~ good", "http://myapp.com/callback");
    service.update(dev, clientApp.getClientId(), "appU2", "poor baby", "myapp://callback");
    ClientApp loaded = service.loadClientAppWithoutCache(clientApp.getClientId());
    assertEquals("appU2", loaded.getAppName());
    assertEquals("poor baby", loaded.getDescription());
    assertEquals("myapp://callback", loaded.getCallbackUri());
  }

  @Test
  public void update_not_owner() throws Exception {
    ClientApp clientApp = service.create(dev, "myapp", "ya ~ good", "http://myapp.com/callback");
    try {
      service.update(savedAccountCitizen("otherDev"),
          clientApp.getClientId(),
          "appU2",
          "poor baby",
          "myapp://callback");
      fail("AccessDeniedException expected");
    } catch (AccessDeniedException expected) {
    }
  }

  @Test
  public void create_not_citizen() throws Exception {
    Account tourist = savedAccountTourist("tourist1");
    try {
      service.create(tourist, "myapp", "ya ~ good", "http://myapp.com/callback");
      fail("AccessDeniedException expected");
    } catch (AccessDeniedException expected) {
    }
  }

  @Test
  public void client_app_not_allow_reserved_word() throws Exception {
    try {
      service.create(dev, "myapp", "ya ~ good", "http://myapp.com/kaif");
      fail("CallbackUriReservedException expected");
    } catch (CallbackUriReservedException expected) {
    }
    try {
      service.create(dev, "myapp", "ya ~ good", "Kaif-demo://myapp.com/");
      fail("CallbackUriReservedException expected");
    } catch (CallbackUriReservedException expected) {
    }
    try {
      service.create(dev, "Kaif", "ya ~ good", "demo://myapp.com/");
      fail("ClientAppNameReservedException expected");
    } catch (ClientAppNameReservedException expected) {
    }

    ClientApp clientApp = service.create(dev, "myapp", "ya ~ good", "http://myapp.com/callback");
    try {
      service.update(dev, clientApp.getClientId(), "kaif", "foobar", "demo://foo");
      fail("ClientAppNameReservedException expected");
    } catch (ClientAppNameReservedException expected) {
    }
  }

  @Test
  public void listClientApps() throws Exception {
    ClientApp clientApp1 = service.create(dev, "myapp1", "ya ~ good", "http://myapp1.com/callback");
    ClientApp clientApp2 = service.create(dev, "myapp2", "ya ~ good", "http://myapp2.com/callback");
    assertEquals(asList(clientApp1, clientApp2), service.listClientApps(dev));
  }

  @Test
  public void directGrantCode() throws Exception {
    Account user = savedAccountCitizen("user1");
    AccountOnceToken oauthDirectAuthorizeToken = accountService.createOauthDirectAuthorizeToken(user);
    ClientApp clientApp = service.create(dev, "myapp", "ya ~ good", "http://myapp.com/callback");

    String code = service.directGrantCode(oauthDirectAuthorizeToken.getToken(),
        clientApp.getClientId(),
        "feed public",
        "http://myapp.com/callback/foo");

    assertTrue(code.length() > 100);
    OauthAccessTokenDto token = service.createOauthAccessTokenByGrantCode(code,
        clientApp.getClientId(),
        "http://myapp.com/callback/foo");
    assertNotNull(token.getAccessToken());
    assertEquals("feed public", token.getScope());
  }

  @Test
  public void createOauthAccessTokenByGrantCode() throws Exception {
    Account user = savedAccountCitizen("user1");
    AccountOnceToken oauthDirectAuthorizeToken = accountService.createOauthDirectAuthorizeToken(user);
    ClientApp clientApp = service.create(dev, "myapp", "ya ~ good", "http://myapp.com/callback");
    String grantCode = service.directGrantCode(oauthDirectAuthorizeToken.getToken(),
        clientApp.getClientId(),
        "feed public",
        "http://myapp.com/callback/foo");

    OauthAccessTokenDto tokenDto = service.createOauthAccessTokenByGrantCode(grantCode,
        clientApp.getClientId(),
        "http://myapp.com/callback/foo");
    ClientAppUser appUser = service.listGrantedAppUsers(user).get(0);
    assertEquals(user.getAccountId(), appUser.getAccountId());
    assertEquals(clientApp.getClientId(), appUser.getClientId());
    assertEquals(clientApp.getClientSecret(), appUser.getCurrentClientSecret());
    assertEquals(EnumSet.of(ClientAppScope.FEED, ClientAppScope.PUBLIC),
        appUser.getLastGrantedScopes());
    ClientAppUserAccessToken clientAppUserAccessToken = service.verifyAccessToken(tokenDto.getAccessToken())
        .get();
    assertTrue(clientAppUserAccessToken.validate(appUser));
    assertTrue(clientAppUserAccessToken.containsScope(ClientAppScope.FEED));
    assertTrue(clientAppUserAccessToken.containsScope(ClientAppScope.PUBLIC));
  }

  @Test
  public void listGrantedApps() throws Exception {
    Account user = savedAccountCitizen("user1");
    assertEquals(0, service.listGrantedApps(user).size());
    ClientApp app1 = service.create(dev, "myapp1", "ya ~ good", "http://myapp.com/callback");
    ClientApp app2 = service.create(dev, "myapp2", "ya ~ good", "http://myapp.com/callback");
    service.createOauthAccessToken(app1,
        user.getAccountId(),
        EnumSet.of(ClientAppScope.FEED),
        Duration.ofDays(1)).getAccessToken();
    service.createOauthAccessToken(app2,
        user.getAccountId(),
        EnumSet.of(ClientAppScope.FEED),
        Duration.ofDays(1)).getAccessToken();

    assertEquals(asList(app2, app1), service.listGrantedApps(user));
  }

  @Test
  public void generateDebugAccessToken() throws Exception {
    ClientApp app1 = service.create(dev, "myapp1", "ya ~ good", "http://myapp.com/callback");
    String token = service.generateDebugAccessToken(dev, app1.getClientId());
    assertTrue(service.verifyAccessToken(token).isPresent());
  }

  @Test
  public void verifyAccessToken_failed_if_app_reset_secret() throws Exception {
    Account user = savedAccountCitizen("user1");
    ClientApp clientApp = service.create(dev, "myapp", "ya ~ good", "http://myapp.com/callback");
    String accessToken = service.createOauthAccessToken(clientApp,
        user.getAccountId(),
        EnumSet.of(ClientAppScope.FEED),
        Duration.ofDays(1)).getAccessToken();
    assertTrue(service.verifyAccessToken(accessToken).isPresent());
    service.resetClientAppSecret(dev, clientApp.getClientId());
    assertFalse(service.verifyAccessToken(accessToken).isPresent());
  }

  @Test
  public void validateApp() throws Exception {
    assertFalse(service.validateApp("foo", "bar"));
    ClientApp clientApp = service.create(dev, "myapp", "ya ~ good", "http://myapp.com/callback");
    assertTrue(service.validateApp(clientApp.getClientId(), clientApp.getClientSecret()));
    service.resetClientAppSecret(dev, clientApp.getClientId());
    assertFalse(service.validateApp(clientApp.getClientId(), clientApp.getClientSecret()));
    ClientApp reset = service.loadClientAppWithoutCache(clientApp.getClientId());
    assertTrue(service.validateApp(reset.getClientId(), reset.getClientSecret()));
  }

  @Test
  public void verifyAccessToken_failed_if_user_revoked() throws Exception {
    Account user = savedAccountCitizen("user1");
    ClientApp clientApp = service.create(dev, "myapp", "ya ~ good", "http://myapp.com/callback");
    String accessToken = service.createOauthAccessToken(clientApp,
        user.getAccountId(),
        EnumSet.of(ClientAppScope.FEED),
        Duration.ofDays(1)).getAccessToken();
    assertTrue(service.verifyAccessToken(accessToken).isPresent());
    service.revokeApp(user, clientApp.getClientId());
    assertFalse(service.verifyAccessToken(accessToken).isPresent());
  }

  @Test
  public void createOauthAccessToken_preserve_last_creation_only() throws Exception {
    Account user = savedAccountCitizen("user1");
    ClientApp clientApp = service.create(dev, "myapp", "ya ~ good", "http://myapp.com/callback");

    service.createOauthAccessToken(clientApp,
        user.getAccountId(),
        EnumSet.of(ClientAppScope.FEED),
        Duration.ofDays(1));
    List<ClientAppUser> clientAppUsers = service.listGrantedAppUsers(user);
    assertEquals(1, clientAppUsers.size());
    ClientAppUser appUser = clientAppUsers.get(0);
    assertEquals(user.getAccountId(), appUser.getAccountId());
    assertEquals(clientApp.getClientId(), appUser.getClientId());
    assertEquals(clientApp.getClientSecret(), appUser.getCurrentClientSecret());
    assertEquals(EnumSet.of(ClientAppScope.FEED), appUser.getLastGrantedScopes());

    //both clientSecret and scopes are updated
    service.resetClientAppSecret(dev, clientApp.getClientId());
    ClientApp resetApp = service.loadClientAppWithoutCache(clientApp.getClientId());
    service.createOauthAccessToken(clientApp,
        user.getAccountId(),
        EnumSet.of(ClientAppScope.ARTICLE),
        Duration.ofDays(1));

    clientAppUsers = service.listGrantedAppUsers(user);
    assertEquals("should update exist client app user if issue new access token",
        1,
        clientAppUsers.size());

    ClientAppUser updated = clientAppUsers.get(0);
    assertEquals(user.getAccountId(), updated.getAccountId());
    assertEquals(clientApp.getClientId(), updated.getClientId());
    assertEquals(resetApp.getClientSecret(), updated.getCurrentClientSecret());
    assertEquals(EnumSet.of(ClientAppScope.ARTICLE), updated.getLastGrantedScopes());
  }
}
