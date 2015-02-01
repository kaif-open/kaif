package io.kaif.flake;

import static org.junit.Assert.*;

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