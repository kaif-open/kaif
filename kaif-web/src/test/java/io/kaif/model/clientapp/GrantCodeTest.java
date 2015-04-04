package io.kaif.model.clientapp;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import io.kaif.test.ModelFixture;

public class GrantCodeTest implements ModelFixture {

  private OauthSecret secret;

  @Before
  public void setUp() throws Exception {
    secret = new OauthSecret();
    secret.setKey("KbKouubC8Zg8P2jsy19SMQ");
    secret.setMac("dpLIWEdghZS4XnBsHqHzRQ");
  }

  @Test
  public void codec() throws Exception {
    GrantCode code = new GrantCode(UUID.randomUUID(),
        "cli-id",
        "sec",
        "foo://bar",
        EnumSet.of(ClientAppScope.FEED, ClientAppScope.PUBLIC));

    String token = code.encode(Instant.now().plus(Duration.ofHours(1)), secret);
    assertTrue(token.length() > 100);
    GrantCode decoded = GrantCode.tryDecode(token, secret).get();
    assertEquals(code, decoded);
    assertFalse(GrantCode.tryDecode("bad", secret).isPresent());
  }

  @Test
  public void matches() throws Exception {
    ClientApp app1 = clientApp(accountCitizen("dev1"), "myapp");
    ClientApp app2 = clientApp(accountCitizen("dev1"), "myapp2");

    GrantCode code = new GrantCode(UUID.randomUUID(),
        app1.getClientId(),
        app1.getClientSecret(),
        "redirect://foo",
        EnumSet.of(ClientAppScope.ARTICLE));

    assertTrue(code.matches(app1, "redirect://foo"));
    assertFalse(code.matches(app1, "redirect://wrong"));
    assertFalse(code.matches(app2, "redirect://foo"));
  }
}