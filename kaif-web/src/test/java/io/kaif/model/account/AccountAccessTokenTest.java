package io.kaif.model.account;

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
        EnumSet.of(Authority.NORMAL, Authority.ROOT));

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
    assertEquals(1, AccountAccessToken.authoritiesToInt(EnumSet.of(Authority.NORMAL)));
    assertEquals(3,
        AccountAccessToken.authoritiesToInt(EnumSet.of(Authority.NORMAL, Authority.ZONE_ADMIN)));
    assertEquals(7,
        AccountAccessToken.authoritiesToInt(EnumSet.of(Authority.NORMAL,
            Authority.ZONE_ADMIN,
            Authority.ROOT)));
  }

  @Test
  public void match() throws Exception {
    AccountAccessToken accountAccessToken = new AccountAccessToken(UUID.randomUUID(),
        "pw1",
        EnumSet.of(Authority.NORMAL, Authority.ROOT));

    assertTrue(accountAccessToken.matches("pw1", EnumSet.of(Authority.NORMAL, Authority.ROOT)));
    assertTrue(accountAccessToken.matches("pw1", EnumSet.of(Authority.ROOT, Authority.NORMAL)));
    assertFalse(accountAccessToken.matches("pw1", EnumSet.of(Authority.NORMAL)));
    assertFalse(accountAccessToken.matches("wrongpw1",
        EnumSet.of(Authority.ROOT, Authority.NORMAL)));
  }
}