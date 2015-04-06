package io.kaif.web.v1;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.article.Article;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.MvcIntegrationTests;

public class V1ArticleResourceTest extends MvcIntegrationTests {

  private Account user;
  private ZoneInfo zone;
  private Article article1;
  private Article article2;

  @Before
  public void setUp() throws Exception {
    user = accountCitizen("user1");
    zone = zoneDefault("fun");
    article1 = article(zone.getZone(), "art1");
    article2 = articleSpeak(zone.getZone(), FlakeId.fromString("b11223344"), "art2");
  }

  @Test
  public void hot() throws Exception {
    when(articleService.listTopArticles(null)).thenReturn(asList(article1));
    oauthPerform(user, get("/v1/article/hot")).andExpect(status().isOk())
        // .andDo(print())
        .andExpect(jsonPath("$.data[0].title", is("art1")))
        .andExpect(jsonPath("$.data[0].link", is(notNullValue())))
        .andExpect(jsonPath("$.data[0].content", is(nullValue(String.class))))
        .andExpect(jsonPath("$.data[0].articleType", is("EXTERNAL_LINK")));
  }

  @Test
  public void latest() throws Exception {
    when(articleService.listLatestArticles(FlakeId.fromString("foo2000"))).thenReturn(asList(
        article1,
        article2));
    oauthPerform(user, get("/v1/article/latest").param("start-article-id", "foo2000"))//
        // .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[1].title", is("art2")))
        .andExpect(jsonPath("$.data[1].link", is(nullValue(String.class))))
        .andExpect(jsonPath("$.data[1].content", is(notNullValue())))
        .andExpect(jsonPath("$.data[1].articleType", is("SPEAK")));
  }

  @Test
  public void userSubmitted() throws Exception {
    when(articleService.listArticlesByAuthor("user1", FlakeId.fromString("foo2000"))).thenReturn(
        asList(article1, article2));
    oauthPerform(user,
        get("/v1/article/user/user1/submitted").param("start-article-id", "foo2000"))//
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[1].title", is("art2")))
        .andExpect(jsonPath("$.data[1].articleType", is("SPEAK")));
  }

  @Test
  public void voted() throws Exception {
    when(voteService.listUpVotedArticles(isA(ClientAppUserAccessToken.class),
        isNull(FlakeId.class))).thenReturn(asList(article2));
    oauthPerform(user, get("/v1/article/voted"))//
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].title", is("art2")))
        .andExpect(jsonPath("$.data[0].articleType", is("SPEAK")));
  }

  @Test
  public void latestByZone() throws Exception {
    when(articleService.listLatestZoneArticles(zone.getZone(), null)).thenReturn(asList(article1,
        article2));
    oauthPerform(user, get("/v1/article/zone/fun/latest"))//
        .andExpect(status().isOk()).andExpect(jsonPath("$.data[1].title", is("art2")));
  }

  @Test
  public void hotByZone() throws Exception {
    when(articleService.listHotZoneArticles(zone.getZone(), null)).thenReturn(asList(article1));
    oauthPerform(user, get("/v1/article/zone/fun/hot"))//
        .andExpect(status().isOk()).andExpect(jsonPath("$.data[0].title", is("art1")));
  }

  @Test
  public void article() throws Exception {
    when(articleService.loadArticle(FlakeId.fromString("foo2000"))).thenReturn(article2);
    oauthPerform(user, get("/v1/article/foo2000"))//
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.title", is("art2")));
  }

  @Test
  public void externalLink() throws Exception {
    when(articleService.createExternalLink(isA(ClientAppUserAccessToken.class),
        eq(zone.getZone()),
        eq("java is old"),
        eq("http://google.com"))).thenReturn(article1);

    String body = q("{'title':'java is old','url':'http://google.com','zone':'fun'}");

    oauthPerform(user, put("/v1/article/external-link").content(body))//
        .andExpect(status().isCreated()).andExpect(jsonPath("$.data.title", is("art1")));
  }

  @Test
  public void speak() throws Exception {
    when(articleService.createSpeak(isA(ClientAppUserAccessToken.class),
        eq(zone.getZone()),
        eq("java is old"),
        eq("java8 is new"))).thenReturn(article1);

    String body = q("{'title':'java is old','content':'java8 is new','zone':'fun'}");

    oauthPerform(user, put("/v1/article/speak").content(body))//
        .andExpect(status().isCreated()).andExpect(jsonPath("$.data.title", is("art1")));
  }
}