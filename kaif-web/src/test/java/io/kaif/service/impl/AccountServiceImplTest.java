package io.kaif.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Locale;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.account.AccountAuth;
import io.kaif.model.account.AccountDao;
import io.kaif.model.account.AccountOnceToken;
import io.kaif.model.account.AccountSecret;
import io.kaif.model.account.AccountStats;
import io.kaif.model.account.Authority;
import io.kaif.model.account.Authorization;
import io.kaif.model.exception.OldPasswordNotMatchException;
import io.kaif.test.DbIntegrationTests;
import io.kaif.web.support.AccessDeniedException;

public class AccountServiceImplTest extends DbIntegrationTests {

  @Autowired
  private AccountServiceImpl service;

  @Autowired
  private AccountSecret accountSecret;

  @Autowired
  private AccountDao accountDao;

  private Locale lc = Locale.TAIWAN;

  @Test
  public void createViaEmail() {
    Account account = service.createViaEmail("myname", "foo@gmail.com", "pwd123", lc);
    Account loaded = accountDao.findById(account.getAccountId()).get();
    assertEquals(account, loaded);
    assertEquals("foo@gmail.com", loaded.getEmail());
    assertFalse(loaded.isActivated());
    assertEquals(EnumSet.of(Authority.TOURIST), loaded.getAuthorities());
    verify(mockMailAgent).sendAccountActivation(eq(lc),
        eq(account),
        Mockito.matches("[a-z\\-0-9]{36}"));

    AccountOnceToken token = accountDao.listOnceTokens().get(0);

    assertEquals(account.getAccountId(), token.getAccountId());
    assertEquals(AccountOnceToken.Type.ACTIVATION, token.getTokenType());
    assertFalse(token.isExpired(Instant.now().plus(Duration.ofHours(23))));
    assertFalse(token.isComplete());

    AccountStats stats = service.loadAccountStats(account.getUsername());
    assertEquals(AccountStats.zero(account.getAccountId()), stats);
  }

  @Test
  public void resendActivation() {
    Account account = service.createViaEmail("myname", "foo@gmail.com", "pwd123", lc);

    Mockito.reset(mockMailAgent);

    service.resendActivation(account, lc);

    verify(mockMailAgent).sendAccountActivation(eq(lc),
        eq(account),
        Mockito.matches("[a-z\\-0-9]{36}"));

    assertEquals(2, accountDao.listOnceTokens().size());
  }

  @Test
  public void findValidResetPasswordToken() throws Exception {
    service.createViaEmail("myname", "foo@gmail.com", "pwd123", lc);
    service.sendResetPassword("myname", "foo@gmail.com", lc);
    AccountOnceToken resetToken = accountDao.listOnceTokens().get(1);
    assertTrue(service.findValidResetPasswordToken(resetToken.getToken()).isPresent());

    accountDao.completeOnceToken(resetToken);

    assertFalse(service.findValidResetPasswordToken(resetToken.getToken()).isPresent());
  }

  @Test
  public void updatePasswordWithOnceToken() throws Exception {
    Account account = service.createViaEmail("myname", "foo@gmail.com", "pwd123", lc);
    service.sendResetPassword("myname", "foo@gmail.com", lc);
    AccountOnceToken resetToken = accountDao.listOnceTokens().get(1);

    Mockito.reset(mockMailAgent);

    service.updatePasswordWithOnceToken(resetToken.getToken(), "pwd456", lc);

    verify(mockMailAgent).sendPasswordWasReset(eq(lc), eq(account));

    assertFalse(service.findValidResetPasswordToken(resetToken.getToken()).isPresent());
    assertTrue(service.authenticate("myname", "pwd456").isPresent());

    //update again takes no effect
    service.updatePasswordWithOnceToken(resetToken.getToken(), "pw ignored", lc);
    assertFalse(service.authenticate("myname", "pw ignored").isPresent());
    verifyNoMoreInteractions(mockMailAgent);
  }

  @Test
  public void sendResetPassword_noSuchAccount() {
    //case 1: no account
    service.sendResetPassword("myname", "foo@gmail.com", lc);
    verifyZeroInteractions(mockMailAgent);
    assertEquals(0, accountDao.listOnceTokens().size());
  }

  @Test
  public void sendResetPassword_emailNotMatch() {
    service.createViaEmail("myname", "foo@gmail.com", "pwd123", lc);
    Mockito.reset(mockMailAgent);

    service.sendResetPassword("myname", "wrong@gmail.com", lc);
    verifyZeroInteractions(mockMailAgent);
    assertEquals(1, accountDao.listOnceTokens().size());
  }

  @Test
  public void sendResetPassword() {
    Account account = service.createViaEmail("myname", "foo@gmail.com", "pwd123", lc);

    Mockito.reset(mockMailAgent);

    service.sendResetPassword("myname", "foo@gmail.com", lc);

    verify(mockMailAgent).sendResetPassword(eq(lc),
        eq(account),
        Mockito.matches("[a-z\\-0-9]{36}"));

    assertTrue(accountDao.listOnceTokens()
        .stream()
        .anyMatch(token -> token.getTokenType() == AccountOnceToken.Type.FORGET_PASSWORD));
  }

  @Test
  public void activate() throws Exception {
    Account account = service.createViaEmail("xyz", "xyz@gmail.com", "595959", lc);
    AccountOnceToken token = accountDao.listOnceTokens().get(0);

    assertTrue(service.activate(token.getToken()));
    Account loaded = accountDao.findById(account.getAccountId()).get();
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
    Account loaded = accountDao.findById(account.getAccountId()).get();
    assertFalse(loaded.isActivated());
  }

  @Test
  public void oauthDirectAuthorizeToken() throws Exception {
    Account account = savedAccountCitizen("oauthTest");
    AccountOnceToken token = service.createOauthDirectAuthorizeToken(account);
    assertTrue(service.oauthDirectAuthorize(token.getToken()));
    assertFalse("consumed token should be invalid", service.oauthDirectAuthorize(token.getToken()));
  }

  @Test
  public void oauthDirectAuthorizeToken_skip_expired() throws Exception {
    Account account = savedAccountCitizen("oauthTest");
    service.setClock(Clock.offset(Clock.systemDefaultZone(), Duration.ofHours(-2)));
    AccountOnceToken token = service.createOauthDirectAuthorizeToken(account);
    service.setClock(Clock.systemDefaultZone());
    assertFalse("expired token should be invalid", service.oauthDirectAuthorize(token.getToken()));
  }

  @Test
  public void isNameAvailable() throws Exception {
    assertTrue(service.isUsernameAvailable("xyz123"));
    service.createViaEmail("xyz123", "foobar@gmail.com", "9999123", lc);

    assertFalse(service.isUsernameAvailable("xyz123"));
    assertFalse(service.isUsernameAvailable("XYZ123"));
  }

  @Test
  public void isEmailAvailable() throws Exception {
    assertTrue(service.isEmailAvailable("xyz123@foo.com"));
    service.createViaEmail("xyz123", "xyz123@foo.com", "9999123", lc);

    assertFalse(service.isEmailAvailable("xyz123@foo.com"));
    assertFalse(service.isEmailAvailable("XYZ123@Foo.com"));
  }

  @Test
  public void authenticate_case_insensitive() {
    Instant now = Instant.now();
    service.setClock(Clock.fixed(now, ZoneOffset.UTC));

    service.createViaEmail("myName", "foo@gmail.com", "pwd123", lc);
    AccountAuth auth = service.authenticate("myname", "pwd123").get();
    assertEquals("myName", auth.getUsername());
  }

  @Test
  public void authenticate() {
    Instant now = Instant.now();
    service.setClock(Clock.fixed(now, ZoneOffset.UTC));

    assertFalse(service.authenticate("notexist", "pwd123").isPresent());
    service.createViaEmail("myname", "foo@gmail.com", "pwd123", lc);
    AccountAuth auth = service.authenticate("myName", "pwd123").get();
    assertEquals("myname", auth.getUsername());
    assertTrue(AccountAccessToken.tryDecode(auth.getAccessToken(), accountSecret).isPresent());
    assertTrue(Instant.ofEpochMilli(auth.getExpireTime()).isAfter(now.plus(Duration.ofDays(7))));
    assertEquals(now.toEpochMilli(), auth.getGenerateTime());
    //failed case
    assertFalse(service.authenticate("myname", "wrong pass").isPresent());
  }

  @Test
  public void strongVerifyAccessToken() throws Exception {
    Account account = service.createViaEmail("abc99", "bar@gmail.com", "pppwww", lc);
    AccountAuth accountAuth = service.authenticate("abc99", "pppwww").get();
    assertTrue(service.strongVerifyAccessToken(accountAuth.getAccessToken()).isPresent());

    //invalid case 1 bad token
    assertFalse(service.strongVerifyAccessToken("badtoken").isPresent());

    //invalid case 2, password changed
    service.updateNewPassword(account, "pppwww", "newPw123", lc);
    assertFalse(service.strongVerifyAccessToken(accountAuth.getAccessToken()).isPresent());

    //invalid case 3, authorities changed
    accountAuth = service.authenticate("abc99", "newPw123").get();
    //use dao to update directly
    accountDao.updateAuthorities(account, EnumSet.of(Authority.SUFFRAGE));
    assertFalse(service.strongVerifyAccessToken(accountAuth.getAccessToken()).isPresent());
  }

  @Test
  public void updateAuthorities() throws Exception {
    Authorization account = service.createViaEmail("abc99", "bar@gmail.com", "pppwww", lc);
    EnumSet<Authority> set = EnumSet.of(Authority.CITIZEN, Authority.SYSOP);
    service.updateAuthorities(account, set);
    assertEquals(set, accountDao.findById(account.authenticatedId()).get().getAuthorities());
  }

  @Test
  public void updateAuthorities_should_not_include_forbidden() throws Exception {
    Authorization authorization = service.createViaEmail("abc99", "bar@gmail.com", "pppwww", lc);
    EnumSet<Authority> set = EnumSet.of(Authority.FORBIDDEN, Authority.SYSOP);
    try {
      service.updateAuthorities(authorization, set);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void updateNewPassword() throws Exception {
    Account account = service.createViaEmail("abc99", "bar@gmail.com", "pppwww", lc);
    Mockito.reset(mockMailAgent);
    AccountAuth accountAuth = service.updateNewPassword(account, "pppwww", "123456", lc);
    verify(mockMailAgent).sendPasswordWasReset(eq(lc), eq(account));
    assertNotNull(accountAuth);
    assertTrue(service.authenticate("abc99", "123456").isPresent());
  }

  @Test(expected = OldPasswordNotMatchException.class)
  public void updateNewPassword_oldPasswordNotMatch() throws Exception {
    Account account = service.createViaEmail("abc99", "bar@gmail.com", "pppwww", lc);
    service.updateNewPassword(account, "wrong old pw", "123456", lc);
  }

  @Test
  public void updateDescription() throws Exception {
    Account account = service.createViaEmail("abc99", "bar@gmail.com", "pppwww", lc);
    assertEquals("", account.getRenderDescription());
    assertEquals("<p>Hi I am a <em>developer</em></p>\n",
        service.updateDescription(account, "Hi I am a *developer*"));
  }

  @Test
  public void loadEditableDescription() throws Exception {
    Account account = service.createViaEmail("abc99", "bar@gmail.com", "pppwww", lc);
    assertEquals("", service.loadEditableDescription(account));
    service.updateDescription(account, "Hi I am a <*developer*>");
    assertEquals("Hi I am a &lt;*developer*&gt;", service.loadEditableDescription(account));
  }

  @Test
  public void extendsAccessToken() throws Exception {
    service.createViaEmail("bbbb99", "bar@gmail.com", "pppwww", lc);
    AccountAuth accountAuth = service.authenticate("bbbb99", "pppwww").get();
    AccountAccessToken accountAccessToken = service.strongVerifyAccessToken(accountAuth.getAccessToken())
        .get();
    AccountAuth extend = service.extendsAccessToken(accountAccessToken);
    assertFalse(extend.equals(accountAuth));
    assertTrue(service.strongVerifyAccessToken(extend.getAccessToken()).isPresent());
  }

  @Test
  public void extendsAccessToken_verify_db_failed() throws Exception {
    Account account = service.createViaEmail("bbbb99", "bar@gmail.com", "pppwww", lc);
    AccountAuth accountAuth = service.authenticate("bbbb99", "pppwww").get();
    AccountAccessToken outOfDateToken = service.strongVerifyAccessToken(accountAuth.getAccessToken())
        .get();
    service.updateNewPassword(account, "pppwww", "pw2newone", Locale.ENGLISH);
    try {
      service.extendsAccessToken(outOfDateToken);
      fail("AccessDeniedException expected");
    } catch (AccessDeniedException expected) {
    }
  }

  @Test
  public void loadAccount() {
    Account account = service.createViaEmail("BBbb99", "bar@gmail.com", "pppwww", lc);
    Account loaded = service.loadAccount("BBbb99");
    assertEquals(account, loaded);
    assertEquals("bar@gmail.com", loaded.getEmail());
    assertFalse(loaded.isActivated());
    assertEquals(EnumSet.of(Authority.TOURIST), loaded.getAuthorities());

    loaded = service.loadAccount("Bbbb99");
    assertEquals(account, loaded);

    loaded = service.loadAccount("BbbB99");
    assertEquals(account, loaded);
  }

}