package io.kaif.model.account;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;

import org.junit.Test;

public class AccountAccessTokenTest {

  @Test
  public void codec() throws Exception {
    AccountAccessToken accountAccessToken = new AccountAccessToken("pw1",
        "nam1",
        EnumSet.of(Authority.NORMAL, Authority.ROOT));

    AccountSecret secret = new AccountSecret();
    secret.setKey("KbKouubC8Zg8P2jsy19SMQ");
    secret.setMac("dpLIWEdghZS4XnBsHqHzRQ");
    String token = accountAccessToken.encode(Instant.now().plus(Duration.ofDays(1)), secret);

    assertEquals(accountAccessToken, AccountAccessToken.tryDecode(token, secret).get());
    assertFalse(AccountAccessToken.tryDecode("bad", secret).isPresent());
  }
}