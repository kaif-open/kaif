package io.kaif.model.clientapp;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.model.account.Account;
import io.kaif.test.DbIntegrationTests;

public class ClientAppDaoTest extends DbIntegrationTests {

  @Autowired
  private ClientAppDao dao;
  private ClientApp app;
  private Account user;

  @Before
  public void setUp() throws Exception {
    app = dao.createApp(savedAccountCitizen("dev1"),
        "name1",
        "desc-1",
        "mycallback://bar",
        Instant.now());
    user = savedAccountCitizen("user1");
  }

  @Test
  public void findClientAppUserWithCache_evict_if_merge() throws Exception {
    assertFalse(dao.findClientAppUserWithCache(user.getAccountId(), app.getClientId()).isPresent());
    dao.mergeClientAppUser(user, app, EnumSet.of(ClientAppScope.FEED), Instant.now());
    Optional<ClientAppUser> loaded = dao.findClientAppUserWithCache(user.getAccountId(),
        app.getClientId());
    assertSame(loaded.get(),
        dao.findClientAppUserWithCache(user.getAccountId(), app.getClientId()).get());

    dao.mergeClientAppUser(user,
        app,
        EnumSet.of(ClientAppScope.FEED, ClientAppScope.PUBLIC),
        Instant.now());

    Optional<ClientAppUser> updated = dao.findClientAppUserWithCache(user.getAccountId(),
        app.getClientId());
    assertNotSame(loaded, updated);
    assertTrue(updated.get().getLastGrantedScopes().contains(ClientAppScope.PUBLIC));
  }

  @Test
  public void findClientAppUserWithCache_evict_if_delete() throws Exception {
    dao.mergeClientAppUser(user, app, EnumSet.of(ClientAppScope.FEED), Instant.now());
    Optional<ClientAppUser> loaded = dao.findClientAppUserWithCache(user.getAccountId(),
        app.getClientId());
    dao.deleteClientAppUser(user.getAccountId(), app.getClientId());
    assertFalse(dao.findClientAppUserWithCache(user.getAccountId(), app.getClientId()).isPresent());
  }

  @Test
  public void findClientAppUserWithCache_evict_app_reset_secret() throws Exception {
    dao.mergeClientAppUser(user, app, EnumSet.of(ClientAppScope.FEED), Instant.now());
    Optional<ClientAppUser> loaded = dao.findClientAppUserWithCache(user.getAccountId(),
        app.getClientId());

    ClientApp updatedApp = app.withResetSecret();
    dao.updateAppSecret(updatedApp);
    Optional<ClientAppUser> updated = dao.findClientAppUserWithCache(user.getAccountId(),
        app.getClientId());
    assertNotSame(loaded, updated);
    assertEquals(updatedApp.getClientSecret(), updated.get().getCurrentClientSecret());
  }
}