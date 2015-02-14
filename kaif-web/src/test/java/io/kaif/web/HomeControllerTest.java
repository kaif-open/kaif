package io.kaif.web;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.Test;

import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.MvcIntegrationTests;

public class HomeControllerTest extends MvcIntegrationTests {

  ZoneInfo funZone = zoneDefault("fun");
  ZoneInfo toyZone = zoneDefault("toy");

  @Test
  public void index() throws Exception {
    when(articleService.listTopArticles(null)).thenReturn(//
        asList(article(funZone.getZone(), "joke1")));
    mockMvc.perform(get("/"))
        .andExpect(content().encoding("UTF-8"))
        .andExpect(content().string(containsString("joke1")))
        .andExpect(content().string(containsString("/snapshot/css/kaif.css")))
        .andExpect(content().string(containsString("/snapshot/web/main.dart.js")));
  }

  @Test
  public void listLatestArticles() throws Exception {
    when(articleService.listLatestArticles(null)).thenReturn(//
        asList(article(funZone.getZone(), "joke1"), article(toyZone.getZone(), "gundam")));
    mockMvc.perform(get("/new"))
        .andExpect(content().string(containsString("joke1")))
        .andExpect(content().string(containsString("gundam")));
  }
}