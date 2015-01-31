package io.kaif.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.Test;

import io.kaif.model.zone.ZoneInfo;

public class ZoneControllerTest extends MvcIntegrationTests {

  ZoneInfo zoneInfo = ZoneInfo.createDefault("programming", "fooProgramming", Instant.now());

  @Test
  public void hot() throws Exception {
    when(zoneService.getZone("programming")).thenReturn(zoneInfo);
    mockMvc.perform(get("/z/programming"))
        .andExpect(content().encoding("UTF-8"))
        .andExpect(content().string(containsString("fooProgramming")));
  }

  @Test
  public void hot_redirectFallback() throws Exception {
    when(zoneService.getZone("programming")).thenReturn(zoneInfo);
    mockMvc.perform(get("/z/Programming?xyz"))
        .andExpect(status().isPermanentRedirect())
        .andExpect(redirectedUrl("http://localhost/z/programming?xyz"));
  }
}
