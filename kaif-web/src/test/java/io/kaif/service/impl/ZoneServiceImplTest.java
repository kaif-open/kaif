package io.kaif.service.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.model.account.Account;
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
    ZoneInfo loaded = zoneDao.loadZoneWithoutCache(Zone.valueOf("abc"));
    assertEquals(zoneInfo, loaded);
    assertEquals("abc", loaded.getName());
    assertEquals("Abc", loaded.getAliasName());
    assertEquals(Authority.CITIZEN, loaded.getVoteAuthority());
    assertEquals(Authority.CITIZEN, loaded.getWriteAuthority());
    assertEquals(Authority.CITIZEN, loaded.getDebateAuthority());
    assertEquals(ZoneInfo.THEME_DEFAULT, loaded.getTheme());
    assertFalse(loaded.isHideFromTop());
    assertEquals(0, loaded.getAdminAccountIds().size());
  }

  @Test
  public void createDefault_not_allow_reserve_word() throws Exception {
    try {
      service.createDefault("kaif", "ABC");
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
    }
    try {
      service.createDefault("kaif-abc", "ABC");
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
    }
    try {
      service.createDefault("abckaifabc", "ABC");
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void createKaif() throws Exception {
    ZoneInfo zoneInfo = service.createKaif("faq", "FAQ");
    ZoneInfo loaded = zoneDao.loadZoneWithoutCache(Zone.valueOf("faq"));
    assertEquals(zoneInfo, loaded);
    assertEquals("faq", loaded.getZone().value());
    assertEquals("FAQ", loaded.getAliasName());
    assertEquals(Authority.CITIZEN, loaded.getVoteAuthority());
    assertEquals(Authority.CITIZEN, loaded.getDebateAuthority());
    assertEquals(Authority.FORBIDDEN, loaded.getWriteAuthority());
    assertEquals(ZoneInfo.THEME_KAIF, loaded.getTheme());
    assertTrue(loaded.isHideFromTop());
    assertEquals(0, loaded.getAdminAccountIds().size());
  }

  @Test
  public void getZone_cached() throws Exception {
    service.createDefault("def", "dddd");

    ZoneInfo cached = service.loadZone(Zone.valueOf("def"));
    assertSame(cached, service.loadZone(Zone.valueOf("def")));
  }

  @Test
  public void listCitizenZones() throws Exception {
    ZoneInfo about = service.createDefault("about", "about-alias");
    ZoneInfo abc = service.createDefault("abc", "abc-alias");
    service.createKaif("kaif-java", "java-alias");
    ZoneInfo groovy = service.createDefault("groovy", "groovy-alias");
    assertEquals(asList(abc, about, groovy), service.listCitizenZones());
  }

  @Test
  public void listZoneAtoZ() throws Exception {
    ZoneInfo about = service.createDefault("about", "about-alias");
    ZoneInfo abc = service.createDefault("abc", "abc-alias");
    ZoneInfo java = service.createDefault("java", "java-alias");
    ZoneInfo groovy = service.createDefault("groovy", "groovy-alias");
    Map<String, List<ZoneInfo>> aToZ = service.listZoneAtoZ();

    //key iteration must ordered
    Iterator<String> cat = aToZ.keySet().iterator();
    assertEquals("A", cat.next());
    assertEquals(asList(abc, about), aToZ.get("A"));

    assertEquals("G", cat.next());
    assertEquals(asList(groovy), aToZ.get("G"));

    assertEquals("J", cat.next());
    assertEquals(asList(java), aToZ.get("J"));
  }

  @Test
  public void updateTheme() throws Exception {
    service.createDefault("twfaq", "TW FAQ");
    service.loadZone(Zone.valueOf("twfaq"));//populate cache
    service.updateTheme(Zone.valueOf("twfaq"), ZoneInfo.THEME_KAIF);
    assertEquals(ZoneInfo.THEME_KAIF, service.loadZone(Zone.valueOf("twfaq")).getTheme());
  }

  @Test
  public void listRecommendZones() throws Exception {
    ZoneInfo z1 = service.createDefault("zone1", "Zone 1");
    ZoneInfo z2 = service.createDefault("zone2", "Zone 2");
    ZoneInfo z3 = service.createDefault("zone3", "No article zone");
    ZoneInfo z4 = service.createDefault("zone4", "Zone 4");
    Account account = savedAccountCitizen("foobar");
    IntStream.rangeClosed(1, 10).forEach(i -> {
      savedArticle(z1, account, i + " z1 - title");
      savedArticle(z2, account, i + " z2 - title");
      savedArticle(z4, account, i + " z4 - title");
    });
    assertTrue(service.listRecommendZones().containsAll(asList(z1, z2, z3, z4)));
  }
}