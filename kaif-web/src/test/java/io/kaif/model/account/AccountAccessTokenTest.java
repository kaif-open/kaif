package io.kaif.model.account;

import static io.kaif.model.account.Authority.CITIZEN;
import static io.kaif.model.account.Authority.ROOT;
import static io.kaif.model.account.Authority.TOURIST;
import static io.kaif.model.account.Authority.ZONE_ADMIN;
import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

import org.junit.Test;

public class AccountAccessTokenTest {

  @Test
  public void codec() throws Exception {
    AccountAccessToken accountAccessToken = new AccountAccessToken(UUID.randomUUID(),
        "pw123412356121",
        EnumSet.of(CITIZEN, ROOT));

    AccountSecret secret = new AccountSecret();
    secret.setKey("KbKouubC8Zg8P2jsy19SMQ");
    secret.setMac("dpLIWEdghZS4XnBsHqHzRQ");
    String token = accountAccessToken.encode(Instant.now().plus(Duration.ofDays(1)), secret);
    assertTrue(token.length() > 100);
    assertEquals(accountAccessToken, AccountAccessToken.tryDecode(token, secret).get());
    assertFalse(AccountAccessToken.tryDecode("bad", secret).isPresent());
  }

  @Test
  public void authoritiesToBytes() throws Exception {
    assertEquals(0, AccountAccessToken.authoritiesToInt(EnumSet.noneOf(Authority.class)));
    assertEquals(1, AccountAccessToken.authoritiesToInt(EnumSet.of(TOURIST)));
    assertEquals(3, AccountAccessToken.authoritiesToInt(EnumSet.of(TOURIST, CITIZEN)));
    assertEquals(7, AccountAccessToken.authoritiesToInt(EnumSet.of(TOURIST, CITIZEN, ZONE_ADMIN)));
    assertEquals(15,
        AccountAccessToken.authoritiesToInt(EnumSet.of(TOURIST, CITIZEN, ZONE_ADMIN, ROOT)));
  }

  @Test
  public void match() throws Exception {
    AccountAccessToken accountAccessToken = new AccountAccessToken(UUID.randomUUID(),
        "pw1",
        EnumSet.of(CITIZEN, ROOT));

    assertTrue(accountAccessToken.matches("pw1", EnumSet.of(CITIZEN, ROOT)));
    assertTrue(accountAccessToken.matches("pw1", EnumSet.of(ROOT, CITIZEN)));
    assertFalse(accountAccessToken.matches("pw1", EnumSet.of(CITIZEN)));
    assertFalse(accountAccessToken.matches("wrongpw1", EnumSet.of(ROOT, CITIZEN)));
  }
}