package io.kaif.web;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.Test;

import io.kaif.test.MvcIntegrationTests;

public class ArticleControllerTest extends MvcIntegrationTests {

  @Test
  public void createLink() throws Exception {

    when(zoneService.listCitizenZones()).thenReturn(//
        asList(zoneDefault("zone1"), zoneDefault("zone2")));

    mockMvc.perform(get("/article/create-link"))
        .andExpect(view().name("article/create"))
        .andExpect(content().string(containsString("zone1")))
        .andExpect(content().string(containsString("zone2")))
        .andExpect(content().string(containsString("id=\"urlInput\"")));
  }

  @Test
  public void createSpeak() throws Exception {
    mockMvc.perform(get("/article/create-speak"))
        .andExpect(view().name("article/create"))
        .andExpect(content().string(containsString("id=\"contentInput\"")));
  }

  @Test
  public void bookmarklet() throws Exception {
    mockMvc.perform(//
        get("/article/create-link").param("c", "http://foo.com").param("t", "my_title_abc"))
        .andExpect(content().string(containsString("value=\"http://foo.com\"")))
        .andExpect(content().string(containsString("my_title_abc")));
  }
}