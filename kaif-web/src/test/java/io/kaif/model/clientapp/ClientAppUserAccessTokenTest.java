package io.kaif.model.clientapp;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import io.kaif.model.account.Account;
import io.kaif.model.account.Authority;
import io.kaif.test.ModelFixture;

public class ClientAppUserAccessTokenTest implements ModelFixture {
  private OauthSecret secret;

  @Before
  public void setUp() throws Exception {
    secret = new OauthSecret();
    secret.setKey("KbKouubC8Zg8P2jsy19SMQ");
    secret.setMac("dpLIWEdghZS4XnBsHqHzRQ");
  }

  @Test
  public void codec() throws Exception {
    ClientAppUserAccessToken clientAppUserAccessToken = new ClientAppUserAccessToken(UUID.randomUUID(),
        EnumSet.of(Authority.CITIZEN, Authority.TOURIST),
        EnumSet.of(ClientAppScope.FEED),
        "client-id-foo",
        "secret-client");

    String token = clientAppUserAccessToken.encode(Instant.now().plus(Duration.ofHours(1)), secret);

    assertTrue(token.length() > 100);
    ClientAppUserAccessToken decoded = ClientAppUserAccessToken.tryDecode(token, secret).get();
    assertEquals(decoded, clientAppUserAccessToken);
  }

  @Test
  public void authorization() throws Exception {
    Account account = accountCitizen("user1");
    UUID accountId = account.getAccountId();
    ClientAppUserAccessToken token = new ClientAppUserAccessToken(accountId,
        EnumSet.of(Authority.CITIZEN, Authority.TOURIST),
        EnumSet.of(ClientAppScope.FEED, ClientAppScope.PUBLIC),
        "client-id-bar",
        "secret-123");

    assertTrue(token.containsAuthority(Authority.CITIZEN));
    assertTrue(token.containsAuthority(Authority.TOURIST));
    assertFalse(token.containsAuthority(Authority.SYSOP));

    assertTrue(token.belongToAccount(accountId));
    assertFalse(token.belongToAccount(UUID.randomUUID()));

    assertTrue(token.matches(account));
    assertTrue("client app user do not check account password change",
        token.matches(account.withPasswordHash("different hash")));
    assertFalse(token.matches(account.withAuthorities(EnumSet.of(Authority.TOURIST))));
    assertFalse(token.matches(accountCitizen("diffuser")));
  }

  @Test
  public void containsScope() throws Exception {
    Account account = accountCitizen("user1");
    UUID accountId = account.getAccountId();
    ClientAppUserAccessToken token = new ClientAppUserAccessToken(accountId,
        EnumSet.of(Authority.CITIZEN, Authority.TOURIST),
        EnumSet.of(ClientAppScope.FEED, ClientAppScope.PUBLIC),
        "client-id-bar",
        "secret-123");

    assertTrue(token.containsScope(ClientAppScope.FEED));
    assertTrue(token.containsScope(ClientAppScope.PUBLIC));
    assertFalse(token.containsScope(ClientAppScope.DEBATE));

    ClientAppUser clientAppUser = ClientAppUser.create("client-id-bar",
        "secret-123",
        accountId,
        EnumSet.of(ClientAppScope.FEED),
        Instant.now());
    assertTrue(token.validate(clientAppUser));
  }

  @Test
  public void validate() throws Exception {
    Account account = accountCitizen("user1");
    UUID accountId = account.getAccountId();
    ClientAppUserAccessToken token = new ClientAppUserAccessToken(accountId,
        EnumSet.of(Authority.CITIZEN, Authority.TOURIST),
        EnumSet.of(ClientAppScope.FEED, ClientAppScope.PUBLIC),
        "client-id-bar",
        "secret-123");

    ClientAppUser clientAppUser = ClientAppUser.create("client-id-bar",
        "secret-123",
        accountId,
        EnumSet.of(ClientAppScope.FEED),
        Instant.now());
    assertTrue(token.validate(clientAppUser));

    ClientAppUser diffClientId = ClientAppUser.create("client-id-diff",
        "secret-123",
        accountId,
        EnumSet.of(ClientAppScope.FEED),
        Instant.now());

    ClientAppUser diffClientSecret = ClientAppUser.create("client-id-bar",
        "secret-diff",
        accountId,
        EnumSet.of(ClientAppScope.FEED),
        Instant.now());

    ClientAppUser diffAccount = ClientAppUser.create("client-id-bar",
        "secret-123",
        UUID.randomUUID(),
        EnumSet.of(ClientAppScope.FEED),
        Instant.now());

    assertFalse(token.validate(diffClientId));
    assertFalse(token.validate(diffClientSecret));
    assertFalse(token.validate(diffAccount));
    assertFalse(token.validate(null));
  }
}