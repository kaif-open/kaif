package io.kaif.web;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import io.kaif.model.article.Article;
import io.kaif.model.debate.Debate;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.MvcIntegrationTests;

public class HomeControllerTest extends MvcIntegrationTests {

  ZoneInfo funZone = zoneDefault("fun");
  ZoneInfo toyZone = zoneDefault("toy");

  @Before
  public void setUp() throws Exception {
    when(zoneService.listRecommendZones()).thenReturn(asList(zoneDefault("recommend1"),
        zoneDefault("recommend2")));
  }

  @Test
  public void index() throws Exception {
    when(articleService.listTopArticles(null)).thenReturn(//
        asList(article(funZone.getZone(), "joke xyz 1")));
    mockMvc.perform(get("/"))
        .andExpect(content().encoding("UTF-8"))
        .andExpect(content().string(containsString("joke xyz 1")))
        .andExpect(content().string(containsString("recommend1")))
        .andExpect(content().string(containsString("recommend2")))
        .andExpect(content().string(containsString("/snapshot/css/kaif.css")))
        .andExpect(content().string(containsString("/snapshot/web/main.dart.js")));
  }

  @Test
  public void rssFeed() throws Exception {
    Article article = article(funZone.getZone(), "joke xyz 1");
    when(articleService.listRssTopArticlesWithCache()).thenReturn(singletonList(article));

    final DateTimeFormatter RFC1123_FORMATTER = DateTimeFormatter.ofPattern(
        "EEE, dd MMM yyyy HH:mm:ss zzz").withZone(ZoneId.of("GMT")).withLocale(Locale.US);

    mockMvc.perform(get("/hot.rss"))
        .andExpect(content().encoding("UTF-8"))
        .andExpect(xpath("/rss/channel/title").string("熱門 kaif.io"))
        .andExpect(xpath("/rss/channel/description").string("綜合熱門"))
        .andExpect(xpath("/rss/channel/pubDate").string(RFC1123_FORMATTER.format(article.getCreateTime())))
        .andExpect(xpath("/rss/channel/item[1]/title").string("joke xyz 1"));
  }

  @Test
  public void zones() throws Exception {
    ImmutableMap<String, List<ZoneInfo>> zones = ImmutableMap.of("F",
        asList(funZone, zoneDefault("fortran")),
        "T",
        asList(toyZone, zoneDefault("tcl")));
    when(zoneService.listZoneAtoZ()).thenReturn(zones);

    mockMvc.perform(get("/zone/a-z"))
        .andExpect(content().string(containsString("fortran")))
        .andExpect(content().string(containsString("tcl")));
  }

  @Test
  public void listLatestArticles() throws Exception {
    when(articleService.listLatestArticles(null)).thenReturn(//
        asList(article(funZone.getZone(), "double 00"), article(toyZone.getZone(), "gundam")));
    mockMvc.perform(get("/new"))
        .andExpect(model().attributeExists("recommendZones"))
        .andExpect(content().string(containsString("double 00")))
        .andExpect(content().string(containsString("gundam")));
  }

  @Test
  public void listLatestDebates() throws Exception {
    Article article = article(funZone.getZone(), "article 1");
    Debate debate = debate(article, "123456", null);
    when(articleService.listLatestDebates(null)).thenReturn(//
        singletonList(debate));
    when(articleService.listArticlesByDebates(singletonList(debate.getDebateId()))).thenReturn(
        singletonList(article));
    mockMvc.perform(get("/new-debate"))
        .andExpect(model().attributeExists("recommendZones"))
        .andExpect(content().string(containsString("123456")))
        .andExpect(containsDebateFormTemplate());
  }
}
