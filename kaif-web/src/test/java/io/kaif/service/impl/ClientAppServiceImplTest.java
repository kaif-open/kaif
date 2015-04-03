package io.kaif.service.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.util.stream.IntStream;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.model.account.Account;
import io.kaif.model.clientapp.ClientApp;
import io.kaif.model.exception.CallbackUriReservedException;
import io.kaif.model.exception.ClientAppMaxException;
import io.kaif.model.exception.ClientAppNameReservedException;
import io.kaif.test.DbIntegrationTests;
import io.kaif.web.support.AccessDeniedException;

public class ClientAppServiceImplTest extends DbIntegrationTests {

  @Autowired
  private ClientAppServiceImpl service;

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
}