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
    UUID accoundId = UUID.randomUUID();
    assertTrue(general.canUpVote(accoundId, EnumSet.of(Authority.CITIZEN)));
    assertFalse(general.canUpVote(accoundId, EnumSet.of(Authority.TOURIST)));
    assertFalse(general.canUpVote(accoundId, EnumSet.of(Authority.SYSOP)));

    ZoneInfo managed = general.withAdmins(Arrays.asList(accoundId));

    assertTrue("admin should ignore authority",
        managed.canUpVote(accoundId, EnumSet.noneOf(Authority.class)));

    assertFalse("not admin", managed.canUpVote(UUID.randomUUID(), EnumSet.noneOf(Authority.class)));
  }

  @Test
  public void canWriteArticle() throws Exception {
    ZoneInfo site = ZoneInfo.createKaif("site", "public", Instant.now());
    UUID accoundId = UUID.randomUUID();
    assertTrue(site.canWriteArticle(accoundId, EnumSet.of(Authority.SYSOP)));
    assertFalse(site.canWriteArticle(accoundId, EnumSet.of(Authority.TOURIST)));
    assertFalse(site.canWriteArticle(accoundId, EnumSet.of(Authority.CITIZEN)));

    ZoneInfo managed = site.withAdmins(Arrays.asList(accoundId));

    assertTrue("admin should ignore authority",
        managed.canWriteArticle(accoundId, EnumSet.noneOf(Authority.class)));

    assertFalse("not admin",
        managed.canWriteArticle(UUID.randomUUID(), EnumSet.noneOf(Authority.class)));
  }

  @Test
  public void canDownVote() throws Exception {
    ZoneInfo zone = ZoneInfo.createDefault("zone-b", "public", Instant.now());
    UUID accoundId = UUID.randomUUID();
    assertTrue(zone.canDownVote(accoundId, EnumSet.of(Authority.CITIZEN)));
    assertFalse(zone.canDownVote(accoundId, EnumSet.of(Authority.TOURIST)));
    assertFalse(zone.canDownVote(accoundId, EnumSet.of(Authority.SYSOP)));

    ZoneInfo managed = zone.withAdmins(Arrays.asList(accoundId));

    assertTrue("admin should ignore authority",
        managed.canDownVote(accoundId, EnumSet.noneOf(Authority.class)));

    ZoneInfo disabledDownVote = zone.withAllowDownVote(false);

    assertFalse(disabledDownVote.canDownVote(accoundId, EnumSet.allOf(Authority.class)));
    assertFalse(disabledDownVote.canDownVote(UUID.randomUUID(), EnumSet.allOf(Authority.class)));
  }
}