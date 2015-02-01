package io.kaif.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.MvcIntegrationTests;

public class ZoneControllerTest extends MvcIntegrationTests {

  ZoneInfo zoneInfo = zoneDefault("programming");

  @Test
  public void hot() throws Exception {
    when(zoneService.getZone(Zone.valueOf("programming"))).thenReturn(zoneInfo);
    mockMvc.perform(get("/z/programming"))
        .andExpect(content().encoding("UTF-8"))
        .andExpect(content().string(containsString("programming-alias")));
  }

  @Test
  public void hot_redirectFallback() throws Exception {
    when(zoneService.getZone(Zone.valueOf("programming"))).thenReturn(zoneInfo);
    mockMvc.perform(get("/z/Programming?xyz"))
        .andExpect(status().isPermanentRedirect())
        .andExpect(redirectedUrl("http://localhost/z/programming?xyz"));
  }

  @Test
  public void notExistZone_404() throws Exception {
    when(zoneService.getZone(Zone.valueOf("not-exist"))).thenThrow(new EmptyResultDataAccessException(
        "fake",
        1));
    mockMvc.perform(get("/z/not-exist"))
        .andExpect(status().isNotFound())
        .andExpect(view().name("error"))
        .andExpect(content().string(containsString("404")));
  }

  @Test
  public void invalidZone_404() throws Exception {
    mockMvc.perform(get("/z/BAD!!!NAME"))
        .andExpect(status().isNotFound())
        .andExpect(view().name("error"))
        .andExpect(content().string(containsString("404")));
  }
}
