package io.kaif.service.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.util.stream.IntStream;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountOnceToken;
import io.kaif.model.clientapp.ClientApp;
import io.kaif.model.exception.CallbackUriReservedException;
import io.kaif.model.exception.ClientAppMaxException;
import io.kaif.model.exception.ClientAppNameReservedException;
import io.kaif.service.AccountService;
import io.kaif.test.DbIntegrationTests;
import io.kaif.web.support.AccessDeniedException;
import io.kaif.oauth.OauthAccessTokenDto;

public class ClientAppServiceImplTest extends DbIntegrationTests {

  @Autowired
  private ClientAppServiceImpl service;
  @Autowired
  private AccountService accountService;

  @Test
  public void create() throws Exception {
    Account dev = savedAccountCitizen("dev1");
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
    Account dev = savedAccountCitizen("dev1");
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
    Account dev = savedAccountCitizen("dev1");
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
    Account dev = savedAccountCitizen("dev1");
    ClientApp clientApp = service.create(dev, "myapp", "ya ~ good", "http://myapp.com/callback");
    service.update(dev, clientApp.getClientId(), "appU2", "poor baby", "myapp://callback");
    ClientApp loaded = service.loadClientAppWithoutCache(clientApp.getClientId());
    assertEquals("appU2", loaded.getAppName());
    assertEquals("poor baby", loaded.getDescription());
    assertEquals("myapp://callback", loaded.getCallbackUri());
  }

  @Test
  public void update_not_owner() throws Exception {
    Account dev = savedAccountCitizen("dev1");
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
    Account tourist = savedAccountTourist("dev1");
    try {
      service.create(tourist, "myapp", "ya ~ good", "http://myapp.com/callback");
      fail("AccessDeniedException expected");
    } catch (AccessDeniedException expected) {
    }
  }

  @Test
  public void client_app_not_allow_reserved_word() throws Exception {
    Account dev = savedAccountCitizen("dev1");
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
    Account dev = savedAccountCitizen("dev1");
    ClientApp clientApp1 = service.create(dev, "myapp1", "ya ~ good", "http://myapp1.com/callback");
    ClientApp clientApp2 = service.create(dev, "myapp2", "ya ~ good", "http://myapp2.com/callback");
    assertEquals(asList(clientApp1, clientApp2), service.listClientApps(dev));
  }

  @Test
  public void directGrantCode() throws Exception {
    Account user = savedAccountCitizen("user1");
    AccountOnceToken oauthDirectAuthorizeToken = accountService.createOauthDirectAuthorizeToken(user);
    Account dev = savedAccountCitizen("dev1");
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
}