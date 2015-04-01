package io.kaif.service.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.model.account.Account;
import io.kaif.model.clientapp.ClientApp;
import io.kaif.test.DbIntegrationTests;

public class ClientAppServiceImplTest extends DbIntegrationTests {

  @Autowired
  private ClientAppServiceImpl service;

  @Test
  public void create() throws Exception {
    Account dev = savedAccountCitizen("dev1");
    ClientApp clientApp = service.create(dev, "myapp", "ya ~ good", "http://myapp.com/callback");
    ClientApp loaded = service.loadClientAppWithoutCache(clientApp.getClientId());
    assertEquals("myapp", clientApp.getAppName());
    assertEquals("ya ~ good", clientApp.getDescription());
    assertEquals("http://myapp.com/callback", clientApp.getCallbackUri());
    assertNotNull(clientApp.getClientSecret());
    assertEquals(dev.getAccountId(), clientApp.getOwnerAccountId());
    assertNotNull(clientApp.getCreateTime());
  }
}