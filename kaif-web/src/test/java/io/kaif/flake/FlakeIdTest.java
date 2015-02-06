package io.kaif.flake;

import static org.junit.Assert.*;

import java.time.Instant;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FlakeIdTest {

  static class FooPojo {
    public FlakeId flakeId;

    public FooPojo(FlakeId flakeId) {
      this.flakeId = flakeId;
    }

    public FooPojo() {
    }
  }

  public static void main(String[] args) {
    String name = "mdformat";
    FlakeId flakeId = FlakeId.fromString(name);
    if (!flakeId.toString().equals(name)) {
      throw new RuntimeException("input name: "
          + name
          + " revert back to flakeId string not match: "
          + flakeId.toString());
    }
    if (name.length() > 8) {
      throw new RuntimeException("name should not > 8 length (production article/debates is >= 9");
    }
    System.out.printf("name convert '%s' to flakeId %d (date: %s) \n",
        flakeId,
        flakeId.value(),
        Instant.ofEpochMilli(flakeId.epochMilli()));
  }

  @Test
  public void json() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    assertEquals("\"gVKJo\"", mapper.writeValueAsString(FlakeId.valueOf(100000000L)));
    assertEquals(FlakeId.fromString("foo"), mapper.readValue("\"foo\"", FlakeId.class));

    assertEquals("{\"flakeId\":\"vZF7Nk\"}",
        mapper.writeValueAsString(new FooPojo(FlakeId.valueOf(20000000000L))));
    assertEquals(FlakeId.fromString("xyz"),
        mapper.readValue("{\"flakeId\":\"xyz\"}", FooPojo.class).flakeId);
  }

}