package io.kaif.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.Test;

import io.kaif.test.MvcIntegrationTests;

public class HomeControllerTest extends MvcIntegrationTests {
  @Test
  public void dynamicRes() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(content().encoding("UTF-8"))
        .andExpect(content().string(containsString("/snapshot/web/kaif.css")))
        .andExpect(content().string(containsString("/snapshot/web/main.dart.js")));
  }
}