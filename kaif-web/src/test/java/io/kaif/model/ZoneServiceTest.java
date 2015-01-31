package io.kaif.model;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.database.DbIntegrationTests;
import io.kaif.model.account.Authority;
import io.kaif.model.zone.ZoneDao;
import io.kaif.model.zone.ZoneInfo;

public class ZoneServiceTest extends DbIntegrationTests {

  @Autowired
  private ZoneService service;

  @Autowired
  private ZoneDao zoneDao;

  @Test
  public void create() throws Exception {
    ZoneInfo zone = service.create("abc",
        "Abc",
        ZoneInfo.THEME_DEFAULT,
        Authority.TOURIST,
        Authority.CITIZEN);

    ZoneInfo loaded = zoneDao.getZoneWithoutCache("abc");
    assertEquals(zone, loaded);
    assertEquals("abc", loaded.getZone());
    assertEquals("Abc", loaded.getAliasName());
    assertEquals(Authority.TOURIST, loaded.getReadAuthority());
    assertEquals(Authority.CITIZEN, loaded.getWriteAuthority());
    assertEquals(ZoneInfo.THEME_DEFAULT, loaded.getTheme());
    assertEquals(0, loaded.getAdminAccountIds().size());
  }

  @Test
  public void getZone_cached() throws Exception {
    service.create("def", "dddd", ZoneInfo.THEME_DEFAULT, Authority.TOURIST, Authority.CITIZEN);

    ZoneInfo cached = service.getZone("def");
    assertSame(cached, service.getZone("def"));
  }

  @Test
  public void updateTheme() throws Exception {
    service.create("twfaq", "TW FAQ", ZoneInfo.THEME_DEFAULT, Authority.TOURIST, Authority.CITIZEN);
    service.getZone("twfaq");//populate cache
    service.updateTheme("twfaq", ZoneInfo.THEME_KAIF);
    assertEquals(ZoneInfo.THEME_KAIF, service.getZone("twfaq").getTheme());
  }
}