package io.kaif.model.zone;

import static org.junit.Assert.*;

import java.time.Instant;

import org.junit.Test;

import io.kaif.model.account.Authority;

public class ZoneInfoTest {

  @Test
  public void zoneFallback() throws Exception {
    assertEquals("foo", ZoneInfo.zoneFallback("foo"));
    assertEquals("foo", ZoneInfo.zoneFallback("Foo"));
    //fallback do not handle space, because url may use %20
    assertEquals("  foo ", ZoneInfo.zoneFallback("  foo "));
    assertEquals("a-b-cd-e", ZoneInfo.zoneFallback("a--b__cd-e"));
    assertEquals("", ZoneInfo.zoneFallback(null));
    assertEquals(" ", ZoneInfo.zoneFallback(" "));
  }

  @Test
  public void nameValidation() throws Exception {
    assertEquals("abc", zoneInfo("abc").getZone());
    assertEquals("111111111", zoneInfo("111111111").getZone());

    assertInvalidZone(null);
    assertInvalidZone("");
    assertInvalidZone("   ");
    assertInvalidZone("a");
    assertInvalidZone("ab");
    assertInvalidZone("1234567890123456789012345678901");
    assertInvalidZone("a__b");
    assertInvalidZone("+++ab");
    assertInvalidZone("-ab");
    assertInvalidZone("ab-");
    assertInvalidZone("a--b");
    assertInvalidZone("a----b");
  }

  private void assertInvalidZone(String zone) {
    try {
      zoneInfo(zone);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
    }
  }

  private ZoneInfo zoneInfo(String zone) {
    return ZoneInfo.create(zone,
        zone,
        ZoneInfo.THEME_DEFAULT,
        Authority.CITIZEN,
        Authority.TOURIST,
        Instant.now());
  }
}