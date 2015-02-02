package io.kaif.model.zone;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

import org.junit.Test;

import io.kaif.model.account.Authority;

public class ZoneInfoTest {

  private ZoneInfo zoneInfo(String zone) {
    return ZoneInfo.createDefault(zone, zone, Instant.now());
  }

  @Test
  public void canUpVote() throws Exception {
    ZoneInfo general = ZoneInfo.createDefault("general", "public", Instant.now());
    UUID accountId = UUID.randomUUID();
    assertTrue(general.canUpVote(accountId, EnumSet.of(Authority.CITIZEN)));
    assertFalse(general.canUpVote(accountId, EnumSet.of(Authority.TOURIST)));
    assertFalse(general.canUpVote(accountId, EnumSet.of(Authority.SYSOP)));

    ZoneInfo managed = general.withAdmins(Arrays.asList(accountId));

    assertTrue("admin should ignore authority",
        managed.canUpVote(accountId, EnumSet.noneOf(Authority.class)));

    assertFalse("not admin", managed.canUpVote(UUID.randomUUID(), EnumSet.noneOf(Authority.class)));
  }

  @Test
  public void canDebate() throws Exception {
    ZoneInfo general = ZoneInfo.createDefault("general", "public", Instant.now());
    UUID accountId = UUID.randomUUID();
    assertTrue(general.canDebate(accountId, EnumSet.of(Authority.CITIZEN)));
    assertFalse(general.canDebate(accountId, EnumSet.of(Authority.TOURIST)));
    assertFalse(general.canDebate(accountId, EnumSet.of(Authority.SYSOP)));

    ZoneInfo managed = general.withAdmins(Arrays.asList(accountId));

    assertTrue("admin should ignore authority",
        managed.canDebate(accountId, EnumSet.noneOf(Authority.class)));

    assertFalse("not admin", managed.canDebate(UUID.randomUUID(), EnumSet.noneOf(Authority.class)));
  }

  @Test
  public void canWriteArticle() throws Exception {
    ZoneInfo site = ZoneInfo.createKaif("site", "public", Instant.now());
    UUID accountId = UUID.randomUUID();
    assertFalse(site.canWriteArticle(accountId, EnumSet.of(Authority.SYSOP)));
    assertFalse(site.canWriteArticle(accountId, EnumSet.of(Authority.TOURIST)));
    assertFalse(site.canWriteArticle(accountId, EnumSet.of(Authority.CITIZEN)));

    ZoneInfo managed = site.withAdmins(Arrays.asList(accountId));

    assertTrue("admin should ignore authority",
        managed.canWriteArticle(accountId, EnumSet.noneOf(Authority.class)));

    assertFalse("not admin",
        managed.canWriteArticle(UUID.randomUUID(), EnumSet.noneOf(Authority.class)));
  }

}