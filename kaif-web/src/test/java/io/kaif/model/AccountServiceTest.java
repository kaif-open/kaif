package io.kaif.model;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Locale;
import java.util.UUID;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.database.DbIntegrationTests;
import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.account.AccountAuth;
import io.kaif.model.account.AccountDao;
import io.kaif.model.account.AccountOnceToken;
import io.kaif.model.account.AccountSecret;
import io.kaif.model.account.Authority;

public class AccountServiceTest extends DbIntegrationTests {

  @Autowired
  private AccountService service;

  @Autowired
  private AccountSecret accountSecret;

  @Autowired
  private AccountDao accountDao;

  private Locale lc = Locale.TAIWAN;

  @Test
  public void createViaEmail() {
    Account account = service.createViaEmail("myname", "foo@gmail.com", "pwd123", lc);
    Account loaded = service.findById(account.getAccountId());
    assertEquals(account, loaded);
    assertEquals("foo@gmail.com", loaded.getEmail());
    assertFalse(loaded.isActivated());
    assertEquals(EnumSet.of(Authority.TOURIST), loaded.getAuthorities());
    verify(mockMailAgent).sendAccountActivation(eq(lc),
        eq(account),
        Mockito.matches("[a-z\\-0-9]{36}"));

    AccountOnceToken token = accountDao.listOnceTokens().get(0);

    assertEquals(account.getAccountId(), token.getAccountId());
    assertEquals(AccountOnceToken.Type.ACTIVATION, token.getType());
    assertFalse(token.isExpired(Instant.now().plus(Duration.ofHours(23))));
    assertFalse(token.isComplete());
  }

  @Test
  public void resendActivation() {
    Account account = service.createViaEmail("myname", "foo@gmail.com", "pwd123", lc);

    Mockito.reset(mockMailAgent);

    service.resendActivation(account.getAccountId(), lc);

    verify(mockMailAgent).sendAccountActivation(eq(lc),
        eq(account),
        Mockito.matches("[a-z\\-0-9]{36}"));

    assertEquals(2, accountDao.listOnceTokens().size());
  }

  @Test
  public void activate() throws Exception {
    Account account = service.createViaEmail("xyz", "xyz@gmail.com", "595959", lc);
    AccountOnceToken token = accountDao.listOnceTokens().get(0);

    assertTrue(service.activate(token.getToken()));
    Account loaded = service.findById(account.getAccountId());
    assertTrue(loaded.isActivated());
    assertTrue(loaded.getAuthorities().contains(Authority.CITIZEN));

    assertFalse("activate twice should invalid", service.activate(token.getToken()));
    assertFalse("not exist token should invalid", service.activate("not exist"));
  }

  @Test
  public void activate_skip_token_expired() throws Exception {
    service.setClock(Clock.offset(Clock.systemDefaultZone(), Duration.ofDays(-2)));
    Account account = service.createViaEmail("xyz", "xyz@gmail.com", "595959", lc);
    AccountOnceToken token = accountDao.listOnceTokens().get(0);

    service.setClock(Clock.systemDefaultZone());
    assertFalse("expired token should invalid", service.activate(token.getToken()));
    Account loaded = service.findById(account.getAccountId());
    assertFalse(loaded.isActivated());
  }

  @Test
  public void isNameAvailable() throws Exception {
    assertTrue(service.isNameAvailable("xyz123"));
    service.createViaEmail("xyz123", "foobar@gmail.com", "9999123", lc);

    assertFalse(service.isNameAvailable("xyz123"));
    assertFalse(service.isNameAvailable("XYZ123"));
  }

  @Test
  public void isEmailAvailable() throws Exception {
    assertTrue(service.isEmailAvailable("xyz123@foo.com"));
    service.createViaEmail("xyz123", "xyz123@foo.com", "9999123", lc);

    assertFalse(service.isEmailAvailable("xyz123@foo.com"));
    assertFalse(service.isEmailAvailable("XYZ123@Foo.com"));
  }

  @Test
  public void authenticate() {
    assertFalse(service.authenticate("notexist", "pwd123").isPresent());
    service.createViaEmail("myname", "foo@gmail.com", "pwd123", lc);
    AccountAuth auth = service.authenticate("myName", "pwd123").get();
    assertEquals("myname", auth.getName());
    assertTrue(AccountAccessToken.tryDecode(auth.getAccessToken(), accountSecret).isPresent());
    assertTrue(Instant.ofEpochMilli(auth.getExpireTime())
        .isAfter(Instant.now().plus(Duration.ofDays(7))));
    //failed case
    assertFalse(service.authenticate("myname", "wrong pass").isPresent());
  }

  @Test
  public void verifyAccessToken() throws Exception {
    Account account = service.createViaEmail("abc99", "bar@gmail.com", "pppwww", lc);
    AccountAuth accountAuth = service.authenticate("abc99", "pppwww").get();
    assertTrue(service.verifyAccessToken(accountAuth.getAccessToken()).isPresent());

    UUID accountId = account.getAccountId();
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
    UUID accountId = service.createViaEmail("abc99", "bar@gmail.com", "pppwww", lc).getAccountId();
    EnumSet<Authority> set = EnumSet.of(Authority.CITIZEN, Authority.ROOT);
    service.updateAuthorities(accountId, set);
    assertEquals(set, service.findById(accountId).getAuthorities());
  }

  @Test
  public void updatePassword() throws Exception {
    UUID accountId = service.createViaEmail("abc99", "bar@gmail.com", "pppwww", lc).getAccountId();

    service.updatePassword(accountId, "pw2123");

    assertTrue(service.authenticate("abc99", "pw2123").isPresent());
    assertFalse(service.authenticate("abc99", "pppwww").isPresent());
  }

  @Test
  public void extendsAccessToken() throws Exception {
    service.createViaEmail("bbbb99", "bar@gmail.com", "pppwww", lc);
    AccountAuth accountAuth = service.authenticate("bbbb99", "pppwww").get();
    AccountAccessToken accountAccessToken = service.verifyAccessToken(accountAuth.getAccessToken())
        .get();
    AccountAuth extend = service.extendsAccessToken(accountAccessToken);
    assertFalse(extend.equals(accountAuth));
    assertTrue(service.verifyAccessToken(extend.getAccessToken()).isPresent());
  }
}