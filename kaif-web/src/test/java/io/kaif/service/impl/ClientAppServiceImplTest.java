package io.kaif.service.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.model.account.Account;
import io.kaif.model.clientapp.ClientApp;
import io.kaif.model.exception.CallbackUriReservedException;
import io.kaif.model.exception.ClientAppNamedReservedException;
import io.kaif.test.DbIntegrationTests;

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
  public void create_not_allow_reserved_word() throws Exception {
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
      fail("ClientAppNamedReservedException expected");
    } catch (ClientAppNamedReservedException expected) {
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