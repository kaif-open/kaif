package io.kaif.model.zone;

import static org.junit.Assert.*;

import java.time.Instant;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.database.DbIntegrationTests;
import io.kaif.model.account.Authority;

public class ZoneDaoTest extends DbIntegrationTests {

  @Autowired
  private ZoneDao dao;

  @Test
  public void create() throws Exception {
    ZoneInfo zone = dao.create("abc",
        "Abc",
        ZoneInfo.THEME_DEFAULT,
        Authority.TOURIST,
        Authority.CITIZEN,
        Instant.now());

    ZoneInfo loaded = dao.getZoneWithoutCache("abc");
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
    ZoneInfo zone = dao.create("def",
        "dddd",
        ZoneInfo.THEME_DEFAULT,
        Authority.TOURIST,
        Authority.CITIZEN,
        Instant.now());

    ZoneInfo cached = dao.getZone("def");
    assertSame(cached, dao.getZone("def"));
  }
}