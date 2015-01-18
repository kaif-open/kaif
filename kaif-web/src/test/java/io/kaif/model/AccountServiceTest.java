package io.kaif.model;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.database.DbIntegrationTests;
import io.kaif.model.account.Account;

public class AccountServiceTest extends DbIntegrationTests {

  @Autowired
  private AccountService service;

  @Test
  public void create() {
    Account account = service.create("foo@gmail.com", "pw123", "myname");
    assertEquals(account, service.findById(account.getAccountId().toString()));
  }
}