package io.kaif.web;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.article.Article;
import io.kaif.model.debate.Debate;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.MvcIntegrationTests;

public class ZoneControllerTest extends MvcIntegrationTests {

  ZoneInfo zoneInfo = zoneDefault("programming");

  @Test
  public void hotArticlesWithPaging() throws Exception {

    Zone z = zoneInfo.getZone();
    when(zoneService.loadZone(z)).thenReturn(zoneInfo);

    Article article1 = article(z, "javascript");
    Article article2 = article(z, FlakeId.fromString("phpone"), "php-lang");

    when(articleService.listHotArticles(z, FlakeId.fromString("123456"))).thenReturn(//
        asList(article1, article2));

    mockMvc.perform(get("/z/programming?start=123456"))
        .andExpect(content().string(containsString("/snapshot/css/z-theme-default.css")))
        .andExpect(content().string(containsString("programming-alias")))
        .andExpect(content().string(containsString("php-lang")))
        .andExpect(content().string(containsString("href=\"/z/programming?start=phpone\"")));
  }

  @Test
  public void newArticles() throws Exception {
    Zone z = zoneInfo.getZone();
    when(zoneService.loadZone(z)).thenReturn(zoneInfo);
    when(articleService.listLatestArticles(z, null)).thenReturn(//
        asList(article(z, "java"), article(z, "ruby"), article(z, "golang")));
    mockMvc.perform(get("/z/programming/new"))
        .andExpect(content().string(containsString("programming-alias")))
        .andExpect(content().string(containsString("java")))
        .andExpect(content().string(containsString("golang")))
        .andExpect(content().string(containsString("ruby")))
        .andExpect(content().string(containsString("moments ago"))); // relativeTime()
  }

  @Test
  public void newArticlesWithPaging() throws Exception {
    Zone z = zoneInfo.getZone();
    when(zoneService.loadZone(z)).thenReturn(zoneInfo);

    Article article1 = article(z, "erlang");
    Article article2 = article(z, FlakeId.fromString("csharp"), "C#");
    when(articleService.listLatestArticles(z, FlakeId.fromString("bcdefg"))).thenReturn(//
        asList(article1, article2));

    mockMvc.perform(get("/z/programming/new?start=bcdefg"))
        .andExpect(content().string(containsString("href=\"/z/programming/new?start=csharp\"")));
  }

  @Test
  public void articleDebates() throws Exception {
    Zone z = zoneInfo.getZone();
    FlakeId articleId = FlakeId.fromString("aaa");
    Article article = article(z, "erlang");
    List<Debate> debates = asList(//
        debate(article, "ERLANG is bad", null), //
        debate(article, "JAVA is better", null));

    when(zoneService.loadZone(z)).thenReturn(zoneInfo);
    when(articleService.loadArticle(z, articleId)).thenReturn(article);
    when(articleService.listHotDebates(z, articleId, 0)).thenReturn(debates);

    mockMvc.perform(get("/z/programming/debates/aaa"))
        .andExpect(view().name("article/debates"))
        .andExpect(content().string(containsString("/snapshot/css/z-theme-default.css")))
        .andExpect(content().string(containsString("programming-alias")))
        .andExpect(content().string(containsString("erlang")))
        .andExpect(content().string(containsString("ERLANG is bad")))
        .andExpect(content().string(containsString("JAVA is better")));
  }

  @Test
  public void hot_redirectFallback() throws Exception {
    when(zoneService.loadZone(Zone.valueOf("programming"))).thenReturn(zoneInfo);
    mockMvc.perform(get("/z/Programming?xyz"))
        .andExpect(status().isPermanentRedirect())
        .andExpect(redirectedUrl("http://localhost/z/programming?xyz"));
  }

  @Test
  public void notExistZone_404() throws Exception {
    when(zoneService.loadZone(Zone.valueOf("not-exist"))).thenThrow(new EmptyResultDataAccessException(
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

  @Test
  public void notAllowCreateArticle() throws Exception {
    Account account = accountTourist("foo");
    String token = prepareAccessToken(account);
    when(zoneService.loadZone(Zone.valueOf("programming"))).thenReturn(zoneInfo);
    mockMvc.perform(//
        get("/z/programming/article/create.part").header(AccountAccessToken.HEADER_KEY, token))
        .andExpect(status().isUnauthorized())
        .andExpect(view().name("access-denied"));
  }
}
