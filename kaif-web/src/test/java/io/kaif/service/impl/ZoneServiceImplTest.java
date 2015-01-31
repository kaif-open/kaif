package io.kaif.service.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.database.DbIntegrationTests;
import io.kaif.service.ZoneService;
import io.kaif.model.account.Authority;
import io.kaif.model.zone.ZoneDao;
import io.kaif.model.zone.ZoneInfo;

public class ZoneServiceImplTest extends DbIntegrationTests {

  @Autowired
  private ZoneService service;

  @Autowired
  private ZoneDao zoneDao;

  @Test
  public void createDefault() throws Exception {
    ZoneInfo zone = service.createDefault("abc", "Abc");
    ZoneInfo loaded = zoneDao.getZoneWithoutCache("abc");
    assertEquals(zone, loaded);
    assertEquals("abc", loaded.getZone());
    assertEquals("Abc", loaded.getAliasName());
    assertEquals(Authority.CITIZEN, loaded.getVoteAuthority());
    assertEquals(Authority.CITIZEN, loaded.getWriteAuthority());
    assertEquals(ZoneInfo.THEME_DEFAULT, loaded.getTheme());
    assertFalse(loaded.isHideFromTop());
    assertTrue(loaded.isAllowDownVote());
    assertEquals(0, loaded.getAdminAccountIds().size());
  }

  @Test
  public void createKaif() throws Exception {
    ZoneInfo zone = service.createKaif("faq", "FAQ");
    ZoneInfo loaded = zoneDao.getZoneWithoutCache("faq");
    assertEquals(zone, loaded);
    assertEquals("faq", loaded.getZone());
    assertEquals("FAQ", loaded.getAliasName());
    assertEquals(Authority.CITIZEN, loaded.getVoteAuthority());
    assertEquals(Authority.SYSOP, loaded.getWriteAuthority());
    assertEquals(ZoneInfo.THEME_KAIF, loaded.getTheme());
    assertTrue(loaded.isHideFromTop());
    assertTrue(loaded.isAllowDownVote());
    assertEquals(0, loaded.getAdminAccountIds().size());
  }

  @Test
  public void getZone_cached() throws Exception {
    service.createDefault("def", "dddd");

    ZoneInfo cached = service.getZone("def");
    assertSame(cached, service.getZone("def"));
  }

  @Test
  public void updateTheme() throws Exception {
    service.createDefault("twfaq", "TW FAQ");
    service.getZone("twfaq");//populate cache
    service.updateTheme("twfaq", ZoneInfo.THEME_KAIF);
    assertEquals(ZoneInfo.THEME_KAIF, service.getZone("twfaq").getTheme());
  }
}