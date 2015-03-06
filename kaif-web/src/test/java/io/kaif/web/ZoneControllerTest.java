package io.kaif.web;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.util.List;

import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.web.servlet.ResultMatcher;

import io.kaif.flake.FlakeId;
import io.kaif.model.article.Article;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateTree;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.MvcIntegrationTests;

public class ZoneControllerTest extends MvcIntegrationTests {

  ZoneInfo zoneInfo = zoneDefault("programming");

  @Test
  public void hotArticlesWithPaging() throws Exception {

    Zone z = zoneInfo.getZone();
    when(zoneService.loadZone(z)).thenReturn(zoneInfo);

    Article article1 = article(z, "javascript discussion");
    Article article2 = article(z, FlakeId.fromString("phpone"), "php-lang discussion");

    when(articleService.listHotZoneArticles(z, FlakeId.fromString("123456"))).thenReturn(//
        asList(article1, article2));

    mockMvc.perform(get("/z/programming?start=123456"))
        .andExpect(content().string(containsString("/snapshot/css/z-theme-default.css")))
        .andExpect(content().string(containsString("programming-alias")))
        .andExpect(content().string(containsString("php-lang")))
        .andExpect(content().string(containsString("href=\"/z/programming?start=phpone\"")));
  }

  @Test
  public void rssFeed() throws Exception {
    Zone z = zoneInfo.getZone();
    when(zoneService.loadZone(z)).thenReturn(zoneInfo);

    Article article1 = article(z, "javascript discussion");
    Article article2 = article(z, FlakeId.fromString("phpone"), "php-lang discussion");

    when(articleService.listHotZoneArticles(z, null)).thenReturn(//
        asList(article1, article2));

    mockMvc.perform(get("/z/programming/hot.rss")).andExpect(xpath("/rss/channel/title").string(
        "programming"))
        //.andExpect(xpath("/rss/channel/description").string("programming-alias 熱門"))
        .andExpect(xpath("/rss/channel/item[1]/title").string("javascript discussion"))
        .andExpect(xpath("/rss/channel/item[2]/guid").string("phpone"));
  }

  @Test
  public void newArticles() throws Exception {
    Zone z = zoneInfo.getZone();
    when(zoneService.loadZone(z)).thenReturn(zoneInfo);
    when(articleService.listLatestZoneArticles(z, null)).thenReturn(//
        asList(article(z, "java 123"), article(z, "ruby 999"), article(z, "golang 456")));
    mockMvc.perform(get("/z/programming/new"))
        .andExpect(content().string(containsString("programming-alias")))
        .andExpect(content().string(containsString("java")))
        .andExpect(content().string(containsString("golang")))
        .andExpect(content().string(containsString("ruby")))
        .andExpect(content().string(containsString("moments ago"))); // relativeTime()
  }

  @Test
  public void newDebates() throws Exception {
    Zone z = zoneInfo.getZone();
    when(zoneService.loadZone(z)).thenReturn(zoneInfo);
    Article a = article(z, "python is serious");
    Debate d1 = debate(a, "agree, good.", null);
    Debate d2 = debate(a, "it's too simple.", null);
    when(articleService.listLatestZoneDebates(z, null)).thenReturn(//
        asList(d1, d2));
    when(articleService.listArticlesByDebates(asList(d1.getDebateId(),
        d2.getDebateId()))).thenReturn(asList(a));
    mockMvc.perform(get("/z/programming/new-debate"))
        .andExpect(content().string(containsString("python is serious")))
        .andExpect(content().string(containsString("agree, good.")))
        .andExpect(content().string(containsString("it&#39;s too simple")))
        .andExpect(containsDebateFormTemplate());
  }

  @Test
  public void newDebatesWithPaging() throws Exception {
    Zone z = zoneInfo.getZone();
    when(zoneService.loadZone(z)).thenReturn(zoneInfo);
    Article a = article(z, "golang must be");
    Debate d1 = debate(a, "cool", null);
    Debate d2 = debate(a, "poor", null);
    when(articleService.listLatestZoneDebates(z, FlakeId.fromString("abcdefghi"))).thenReturn(//
        asList(d1, d2));
    when(articleService.listArticlesByDebates(asList(d1.getDebateId(),
        d2.getDebateId()))).thenReturn(asList(a));
    mockMvc.perform(get("/z/programming/new-debate").param("start", "abcdefghi"))
        .andExpect(content().string(containsString("cool")))
        .andExpect(content().string(containsString("poor")));
  }

  @Test
  public void newArticlesWithPaging() throws Exception {
    Zone z = zoneInfo.getZone();
    when(zoneService.loadZone(z)).thenReturn(zoneInfo);

    Article article1 = article(z, "erlang discussion");
    Article article2 = article(z, FlakeId.fromString("csharp"), "C# too many features");
    when(articleService.listLatestZoneArticles(z, FlakeId.fromString("bcdefg"))).thenReturn(//
        asList(article1, article2));

    mockMvc.perform(get("/z/programming/new?start=bcdefg"))
        .andExpect(content().string(containsString("href=\"/z/programming/new?start=csharp\"")));
  }

  @Test
  public void articleDebates() throws Exception {
    Zone z = zoneInfo.getZone();
    FlakeId articleId = FlakeId.fromString("aaa");
    Article article = article(z, "erlang discussion");
    List<Debate> debates = asList(//
        debate(article, "ERLANG is bad", null), //
        debate(article, "JAVA is better", null));

    when(zoneService.loadZone(z)).thenReturn(zoneInfo);
    when(articleService.loadArticle(articleId)).thenReturn(article);
    when(articleService.listBestDebates(articleId, null)).thenReturn(DebateTree.fromDepthFirst(
        debates));

    mockMvc.perform(get("/z/programming/debates/aaa"))
        .andExpect(view().name("article/debates"))
        .andExpect(content().string(containsString("/snapshot/css/z-theme-default.css")))
        .andExpect(content().string(containsString("programming-alias")))
        .andExpect(content().string(containsString("erlang discussion")))
        .andExpect(content().string(containsString("ERLANG is bad")))
        .andExpect(content().string(containsString("JAVA is better")))
        .andExpect(containsDebateFormTemplate());
  }

  @Test
  public void articleDebates_speakArticle() throws Exception {
    Zone z = zoneInfo.getZone();
    FlakeId articleId = FlakeId.fromString("aaa");
    Article article = articleSpeak(z, articleId, "erlang");
    List<Debate> debates = asList(//
        debate(article, "ERLANG is bad", null));

    when(zoneService.loadZone(z)).thenReturn(zoneInfo);
    when(articleService.loadArticle(articleId)).thenReturn(article);
    when(articleService.listBestDebates(articleId, null)).thenReturn(DebateTree.fromDepthFirst(
        debates));

    mockMvc.perform(get("/z/programming/debates/aaa"))
        .andExpect(view().name("article/debates"))
        .andExpect(content().string(containsString("erlang-content")))
        .andExpect(content().string(containsString("ERLANG is bad")));
  }

  @Test
  public void childDebates() throws Exception {
    Zone z = zoneInfo.getZone();
    FlakeId articleId = FlakeId.fromString("aaa");
    Article article = article(z, "erlang discussion");

    Debate parentDebate = debate(article, "use the right tool", null);

    List<Debate> debates = asList(//
        debate(article, "ERLANG is bad", null), //
        debate(article, "JAVA is better", null));

    when(zoneService.loadZone(z)).thenReturn(zoneInfo);
    when(articleService.loadArticle(articleId)).thenReturn(article);
    when(articleService.loadDebate(parentDebate.getDebateId())).thenReturn(parentDebate);
    when(articleService.listBestDebates(articleId, parentDebate.getDebateId())).thenReturn(
        DebateTree.fromDepthFirst(debates));

    mockMvc.perform(get("/z/programming/debates/aaa/" + parentDebate.getDebateId()))
        .andExpect(view().name("article/debates"))
        .andExpect(content().string(containsString("回上層")))
        .andExpect(content().string(containsString("use the right tool")))
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
  public void createLink() throws Exception {
    when(zoneService.loadZone(Zone.valueOf("programming"))).thenReturn(zoneInfo);
    mockMvc.perform(get("/z/programming/article/create-link"))
        .andExpect(view().name("article/create"))
        .andExpect(content().string(containsString("id=\"urlInput\"")));
  }

  @Test
  public void createSpeak() throws Exception {
    when(zoneService.loadZone(Zone.valueOf("programming"))).thenReturn(zoneInfo);
    ZoneInfo another = zoneDefault("another");
    when(zoneService.listRecommendZones()).thenReturn(asList(zoneInfo, another));
    mockMvc.perform(get("/z/programming/article/create-speak"))
        .andExpect(view().name("article/create"))
        .andExpect(model().attribute("candidateZoneInfos", hasItem(another)))
        .andExpect(model().attribute("candidateZoneInfos", not(hasItem(zoneInfo))))
        .andExpect(content().string(containsString("id=\"contentInput\"")));
  }
}
