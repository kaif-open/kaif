package io.kaif.model.zone;

import static io.kaif.model.account.Authority.CITIZEN;
import static io.kaif.model.account.Authority.SYSOP;
import static io.kaif.model.account.Authority.TOURIST;
import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.UUID;

import org.junit.Test;

import io.kaif.model.account.Authority;
import io.kaif.model.account.Authorization;

public class ZoneInfoTest {

  private static class ConstAuthorization implements Authorization {
    private final UUID accountId;
    private final EnumSet<Authority> authorities;

    public ConstAuthorization(UUID accountId, EnumSet<Authority> authorities) {
      this.accountId = accountId;
      this.authorities = authorities;
    }

    @Override
    public boolean belongToAccounts(Collection<UUID> accountIds) {
      return accountIds.contains(accountId);
    }

    @Override
    public boolean containsAuthority(Authority authority) {
      return authorities.contains(authority);
    }
  }

  private static Authorization auth(UUID accoundId, EnumSet<Authority> authorities) {
    return new ConstAuthorization(accoundId, authorities);
  }

  private ZoneInfo zoneInfo(String zone) {
    return ZoneInfo.createDefault(zone, zone, Instant.now());
  }

  @Test
  public void canUpVote() throws Exception {
    ZoneInfo general = ZoneInfo.createDefault("general", "public", Instant.now());
    UUID accountId = UUID.randomUUID();
    assertTrue(general.canUpVote(auth(accountId, EnumSet.of(CITIZEN))));
    assertFalse(general.canUpVote(auth(accountId, EnumSet.of(TOURIST))));
    assertFalse(general.canUpVote(auth(accountId, EnumSet.of(SYSOP))));

    ZoneInfo managed = general.withAdmins(Arrays.asList(accountId));

    assertTrue("admin should ignore authority",
        managed.canUpVote(auth(accountId, EnumSet.noneOf(Authority.class))));

    assertFalse("not admin",
        managed.canUpVote(auth(UUID.randomUUID(), EnumSet.noneOf(Authority.class))));
  }

  @Test
  public void canDebate() throws Exception {
    ZoneInfo general = ZoneInfo.createDefault("general", "public", Instant.now());
    UUID accountId = UUID.randomUUID();
    assertTrue(general.canDebate(auth(accountId, EnumSet.of(CITIZEN))));
    assertFalse(general.canDebate(auth(accountId, EnumSet.of(TOURIST))));
    assertFalse(general.canDebate(auth(accountId, EnumSet.of(SYSOP))));

    ZoneInfo managed = general.withAdmins(Arrays.asList(accountId));

    assertTrue("admin should ignore authority",
        managed.canDebate(auth(accountId, EnumSet.noneOf(Authority.class))));

    assertFalse("not admin",
        managed.canDebate(auth(UUID.randomUUID(), EnumSet.noneOf(Authority.class))));
  }

  @Test
  public void canWriteArticle() throws Exception {
    ZoneInfo site = ZoneInfo.createKaif("site", "public", Instant.now());
    UUID accountId = UUID.randomUUID();
    assertFalse(site.canWriteArticle(auth(accountId, EnumSet.of(SYSOP))));
    assertFalse(site.canWriteArticle(auth(accountId, EnumSet.of(TOURIST))));
    assertFalse(site.canWriteArticle(auth(accountId, EnumSet.of(CITIZEN))));

    ZoneInfo managed = site.withAdmins(Arrays.asList(accountId));

    assertTrue("admin should ignore authority",
        managed.canWriteArticle(auth(accountId, EnumSet.noneOf(Authority.class))));

    assertFalse("not admin",
        managed.canWriteArticle(auth(UUID.randomUUID(), EnumSet.noneOf(Authority.class))));
  }

}