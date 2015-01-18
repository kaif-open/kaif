package io.kaif.model;

import static org.junit.Assert.*;

import java.util.EnumSet;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.database.DbIntegrationTests;
import io.kaif.model.account.Account;
import io.kaif.model.account.Authority;

public class AccountServiceTest extends DbIntegrationTests {

  @Autowired
  private AccountService service;

  @Test
  public void createViaEmail() {
    Account account = service.createViaEmail("myname", "foo@gmail.com", "pw123");
    Account loaded = service.findById(account.getAccountId().toString());
    assertEquals(account, loaded);
    assertEquals("foo@gmail.com", loaded.getEmail());
    assertFalse(loaded.isActivated());
    assertEquals(EnumSet.of(Authority.NORMAL), loaded.getAuthorities());
  }

}