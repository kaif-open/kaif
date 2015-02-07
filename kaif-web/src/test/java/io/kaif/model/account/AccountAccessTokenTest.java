package io.kaif.model.account;

import static io.kaif.model.account.Authority.CITIZEN;
import static io.kaif.model.account.Authority.SUFFRAGE;
import static io.kaif.model.account.Authority.SYSOP;
import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

import org.junit.Test;

import io.kaif.test.ModelFixture;

public class AccountAccessTokenTest implements ModelFixture {

  @Test
  public void codec() throws Exception {
    AccountAccessToken accountAccessToken = new AccountAccessToken(UUID.randomUUID(),
        "pw123412356121",
        EnumSet.of(CITIZEN, SYSOP));

    AccountSecret secret = new AccountSecret();
    secret.setKey("KbKouubC8Zg8P2jsy19SMQ");
    secret.setMac("dpLIWEdghZS4XnBsHqHzRQ");
    String token = accountAccessToken.encode(Instant.now().plus(Duration.ofDays(1)), secret);
    assertTrue(token.length() > 100);
    AccountAccessToken decoded = AccountAccessToken.tryDecode(token, secret).get();
    assertEquals(accountAccessToken, decoded);
    assertTrue(decoded.containsAuthority(SYSOP));
    assertTrue(decoded.containsAuthority(CITIZEN));
    assertFalse(decoded.containsAuthority(SUFFRAGE));

    assertFalse(AccountAccessToken.tryDecode("bad", secret).isPresent());
  }

  @Test
  public void matches() throws Exception {
    Account account = accountWithAuth("pw1", CITIZEN, SYSOP);

    AccountAccessToken accountAccessToken = new AccountAccessToken(account.getAccountId(),
        "pw1",
        EnumSet.of(CITIZEN, SYSOP));

    assertTrue(accountAccessToken.matches(account));

    accountAccessToken = new AccountAccessToken(account.getAccountId(), "pw1", EnumSet.of(CITIZEN));
    assertFalse(accountAccessToken.matches(account));

    accountAccessToken = new AccountAccessToken(account.getAccountId(),
        "pw1",
        EnumSet.of(SUFFRAGE));

    assertFalse(accountAccessToken.matches(account));

    accountAccessToken = new AccountAccessToken(account.getAccountId(),
        "wrongpw1",
        EnumSet.of(CITIZEN, SYSOP));

    assertFalse(accountAccessToken.matches(account));

    Account diffAccount = accountWithAuth("ppww", SYSOP);
    accountAccessToken = new AccountAccessToken(UUID.randomUUID(), "ppww", EnumSet.of(SYSOP));

    assertFalse(accountAccessToken.matches(diffAccount));

  }
}