package io.kaif.model.account;

import static org.junit.Assert.*;

import org.junit.Test;

public class AccountTest {

  @Test
  public void validName() throws Exception {
    assertFalse(Account.isValidName("a"));
    assertFalse(Account.isValidName(null));
    assertFalse(Account.isValidName("abc de"));
    assertFalse(Account.isValidName("1234567890123456"));
    assertTrue(Account.isValidName("abc_de"));
    assertTrue(Account.isValidName("__AAA"));
  }
}