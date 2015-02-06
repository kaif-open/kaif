package io.kaif.model.account;

import static org.junit.Assert.*;

import org.junit.Test;

public class AccountTest {

  @Test
  public void validName() throws Exception {
    assertFalse(Account.isValidUsername("a"));
    assertFalse(Account.isValidUsername(null));
    assertFalse(Account.isValidUsername("null"));
    assertFalse(Account.isValidUsername("NULL"));
    assertFalse(Account.isValidUsername("abc de"));
    assertFalse(Account.isValidUsername("1234567890123456"));
    assertTrue(Account.isValidUsername("abc_de"));
    assertTrue(Account.isValidUsername("__AAA"));
  }
}