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
    assertTrue(site.canWriteArticle(accountId, EnumSet.of(Authority.SYSOP)));
    assertFalse(site.canWriteArticle(accountId, EnumSet.of(Authority.TOURIST)));
    assertFalse(site.canWriteArticle(accountId, EnumSet.of(Authority.CITIZEN)));

    ZoneInfo managed = site.withAdmins(Arrays.asList(accountId));

    assertTrue("admin should ignore authority",
        managed.canWriteArticle(accountId, EnumSet.noneOf(Authority.class)));

    assertFalse("not admin",
        managed.canWriteArticle(UUID.randomUUID(), EnumSet.noneOf(Authority.class)));
  }

  @Test
  public void canDownVote() throws Exception {
    ZoneInfo zone = ZoneInfo.createDefault("zone-b", "public", Instant.now());
    UUID accountId = UUID.randomUUID();
    assertTrue(zone.canDownVote(accountId, EnumSet.of(Authority.CITIZEN)));
    assertFalse(zone.canDownVote(accountId, EnumSet.of(Authority.TOURIST)));
    assertFalse(zone.canDownVote(accountId, EnumSet.of(Authority.SYSOP)));

    ZoneInfo managed = zone.withAdmins(Arrays.asList(accountId));

    assertTrue("admin should ignore authority",
        managed.canDownVote(accountId, EnumSet.noneOf(Authority.class)));

    ZoneInfo disabledDownVote = zone.withAllowDownVote(false);

    assertFalse(disabledDownVote.canDownVote(accountId, EnumSet.allOf(Authority.class)));
    assertFalse(disabledDownVote.canDownVote(UUID.randomUUID(), EnumSet.allOf(Authority.class)));
  }
}