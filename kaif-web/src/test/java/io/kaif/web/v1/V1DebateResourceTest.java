package io.kaif.web.v1;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;

import io.kaif.model.account.Account;
import io.kaif.model.article.Article;
import io.kaif.model.debate.Debate;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.MvcIntegrationTests;

public class V1DebateResourceTest extends MvcIntegrationTests {
  private Account user;
  private ZoneInfo zone;
  private Article article;
  private Debate debate1;
  private Debate debate2;

  @Before
  public void setUp() throws Exception {
    user = accountCitizen("user1");
    zone = zoneDefault("fun");
    article = article(zone.getZone(), "art1");
    debate1 = debate(article, "deb1", null);
    debate2 = debate(article, "deb2", debate1);
  }

  @Test
  public void debate() throws Exception {
    when(articleService.loadDebateWithCache(debate1.getDebateId())).thenReturn(debate1);
    oauthPerform(user, get("/v1/debate/" + debate1.getDebateId())).andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content", is("deb1")));
  }

  @Test
  public void latest() throws Exception {
    when(articleService.listLatestDebates(null)).thenReturn(asList(debate1));
    oauthPerform(user, get("/v1/debate/latest")).andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].content", is("deb1")));
  }

  @Test
  public void latestByZone() throws Exception {
    when(articleService.listLatestZoneDebates(zone.getZone(), debate1.getDebateId())).thenReturn(
        asList(debate1, debate2));
    oauthPerform(user, get("/v1/debate/zone/fun/latest")//
        .param("start-debate-id", debate1.getDebateId().toString())).andExpect(status().isOk())
        .andExpect(jsonPath("$.data[1].content", is("deb2")));
  }

  @Test
  public void userSubmitted() throws Exception {
    when(articleService.listDebatesByDebater("user1", null)).thenReturn(asList(debate1, debate2));
    oauthPerform(user, get("/v1/debate/user/user1/submitted")).andExpect(status().isOk())
        .andExpect(jsonPath("$.data[1].content", is("deb2")));
  }
}