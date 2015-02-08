package io.kaif.model.zone;

import static org.junit.Assert.*;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ZoneTest {
  static class FooPojo {
    public Zone zone;

    public FooPojo(Zone zone) {
      this.zone = zone;
    }

    public FooPojo() {

    }
  }

  @Test
  public void json() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    assertEquals("\"abc\"", mapper.writeValueAsString(Zone.valueOf("abc")));
    assertEquals(Zone.valueOf("foo"), mapper.readValue("\"foo\"", Zone.class));
    assertNull(mapper.readValue("null", Zone.class));
    assertEquals("{\"zone\":\"xyz\"}", mapper.writeValueAsString(new FooPojo(Zone.valueOf("xyz"))));
    assertEquals(Zone.valueOf("xyz"), mapper.readValue("{\"zone\":\"xyz\"}", FooPojo.class).zone);
  }

  @Test
  public void zoneFallback() throws Exception {
    assertEquals("foo", Zone.tryFallback("foo").get().value());
    assertEquals("foo", Zone.tryFallback("Foo").get().value());
    //fallback do not handle space, because url may use %20
    assertFalse(Zone.tryFallback("  foo ").isPresent());
    assertFalse(Zone.tryFallback(" ").isPresent());
    assertFalse(Zone.tryFallback(null).isPresent());

    assertEquals("a-b-cd-e", Zone.tryFallback("a--b__cd-e").get().value());
  }

  @Test
  public void valueValidation() throws Exception {
    assertEquals("abc", Zone.valueOf("abc").value());
    assertEquals("111111111", Zone.valueOf("111111111").value());

    assertInvalidZone(null);
    assertInvalidZone("null");
    assertInvalidZone("NULL");
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

  private void assertInvalidZone(String rawValue) {
    try {
      Zone.valueOf(rawValue);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
    }
  }
}
