package io.kaif.service.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.model.account.Authority;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneDao;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.service.ZoneService;
import io.kaif.test.DbIntegrationTests;

public class ZoneServiceImplTest extends DbIntegrationTests {

  @Autowired
  private ZoneService service;

  @Autowired
  private ZoneDao zoneDao;

  @Test
  public void createDefault() throws Exception {
    ZoneInfo zoneInfo = service.createDefault("abc", "Abc");
    ZoneInfo loaded = zoneDao.getZoneWithoutCache(Zone.valueOf("abc"));
    assertEquals(zoneInfo, loaded);
    assertEquals("abc", loaded.getName());
    assertEquals("Abc", loaded.getAliasName());
    assertEquals(Authority.CITIZEN, loaded.getVoteAuthority());
    assertEquals(Authority.CITIZEN, loaded.getWriteAuthority());
    assertEquals(Authority.CITIZEN, loaded.getDebateAuthority());
    assertEquals(ZoneInfo.THEME_DEFAULT, loaded.getTheme());
    assertFalse(loaded.isHideFromTop());
    assertTrue(loaded.isAllowDownVote());
    assertEquals(0, loaded.getAdminAccountIds().size());
  }

  @Test
  public void createKaif() throws Exception {
    ZoneInfo zoneInfo = service.createKaif("faq", "FAQ");
    ZoneInfo loaded = zoneDao.getZoneWithoutCache(Zone.valueOf("faq"));
    assertEquals(zoneInfo, loaded);
    assertEquals("faq", loaded.getZone().value());
    assertEquals("FAQ", loaded.getAliasName());
    assertEquals(Authority.CITIZEN, loaded.getVoteAuthority());
    assertEquals(Authority.CITIZEN, loaded.getDebateAuthority());
    assertEquals(Authority.SYSOP, loaded.getWriteAuthority());
    assertEquals(ZoneInfo.THEME_KAIF, loaded.getTheme());
    assertTrue(loaded.isHideFromTop());
    assertTrue(loaded.isAllowDownVote());
    assertEquals(0, loaded.getAdminAccountIds().size());
  }

  @Test
  public void getZone_cached() throws Exception {
    service.createDefault("def", "dddd");

    ZoneInfo cached = service.getZone(Zone.valueOf("def"));
    assertSame(cached, service.getZone(Zone.valueOf("def")));
  }

  @Test
  public void updateTheme() throws Exception {
    service.createDefault("twfaq", "TW FAQ");
    service.getZone(Zone.valueOf("twfaq"));//populate cache
    service.updateTheme(Zone.valueOf("twfaq"), ZoneInfo.THEME_KAIF);
    assertEquals(ZoneInfo.THEME_KAIF, service.getZone(Zone.valueOf("twfaq")).getTheme());
  }
}