package io.kaif.model;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.database.DbIntegrationTests;
import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.account.AccountAuth;
import io.kaif.model.account.AccountSecret;
import io.kaif.model.account.Authority;

public class AccountServiceTest extends DbIntegrationTests {

  @Autowired
  private AccountService service;

  @Autowired
  private AccountSecret accountSecret;

  @Test
  public void createViaEmail() {
    Account account = service.createViaEmail("myname", "foo@gmail.com", "pwd123");
    Account loaded = service.findById(account.getAccountId().toString());
    assertEquals(account, loaded);
    assertEquals("foo@gmail.com", loaded.getEmail());
    assertFalse(loaded.isActivated());
  }

  @Test
  public void isNameAvailable() throws Exception {
    assertTrue(service.isNameAvailable("xyz123"));
    service.createViaEmail("xyz123", "foobar@gmail.com", "9999123");

    assertFalse(service.isNameAvailable("xyz123"));
    assertFalse(service.isNameAvailable("XYZ123"));
  }

  @Test
  public void authenticate() {
    assertFalse(service.authenticate("notexist", "pwd123").isPresent());
    service.createViaEmail("myname", "foo@gmail.com", "pwd123");
    AccountAuth auth = service.authenticate("myName", "pwd123").get();
    assertEquals("myname", auth.getName());
    assertTrue(AccountAccessToken.tryDecode(auth.getAccessToken(), accountSecret).isPresent());
    assertEquals(EnumSet.of(Authority.NORMAL), auth.getAuthorities());
    assertTrue(auth.getExpireTime().isAfter(Instant.now().plus(Duration.ofDays(7))));
    //failed case
    assertFalse(service.authenticate("myname", "wrong pass").isPresent());
  }

  @Test
  public void verifyAccessToken() throws Exception {
    Account account = service.createViaEmail("abc99", "bar@gmail.com", "pppwww");
    AccountAuth accountAuth = service.authenticate("abc99", "pppwww").get();
    assertTrue(service.verifyAccessToken(accountAuth.getAccessToken()).isPresent());

    String accountId = account.getAccountId().toString();
    //invalid case 1 bad token
    assertFalse(service.verifyAccessToken("badtoken").isPresent());

    //invalid case 2, password changed
    service.updatePassword(accountId, "newPw123");
    assertFalse(service.verifyAccessToken(accountAuth.getAccessToken()).isPresent());

    //invalid case 3, authorities changed
    accountAuth = service.authenticate("abc99", "newPw123").get();
    service.updateAuthorities(accountId, EnumSet.of(Authority.ZONE_ADMIN));
    assertFalse(service.verifyAccessToken(accountAuth.getAccessToken()).isPresent());
  }

  @Test
  public void updateAuthorities() throws Exception {
    String accountId = service.createViaEmail("abc99", "bar@gmail.com", "pppwww")
        .getAccountId()
        .toString();
    EnumSet<Authority> set = EnumSet.of(Authority.NORMAL, Authority.ROOT);
    service.updateAuthorities(accountId, set);
    assertEquals(set, service.findById(accountId).getAuthorities());
  }

  @Test
  public void updatePassword() throws Exception {
    String accountId = service.createViaEmail("abc99", "bar@gmail.com", "pppwww")
        .getAccountId()
        .toString();

    service.updatePassword(accountId, "pw2123");

    assertTrue(service.authenticate("abc99", "pw2123").isPresent());
    assertFalse(service.authenticate("abc99", "pppwww").isPresent());
  }

  @Test
  public void extendsAccessToken() throws Exception {
    service.createViaEmail("bbbb99", "bar@gmail.com", "pppwww");
    AccountAuth accountAuth = service.authenticate("bbbb99", "pppwww").get();
    AccountAccessToken accountAccessToken = service.verifyAccessToken(accountAuth.getAccessToken())
        .get();
    AccountAuth extend = service.extendsAccessToken(accountAccessToken);
    assertFalse(extend.equals(accountAuth));
    assertTrue(service.verifyAccessToken(extend.getAccessToken()).isPresent());
  }
}