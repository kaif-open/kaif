package io.kaif.web.v1;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.article.Article;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.model.debate.Debate;
import io.kaif.model.vote.ArticleVoter;
import io.kaif.model.vote.DebateVoter;
import io.kaif.model.vote.VoteState;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.MvcIntegrationTests;

public class V1VoteResourceTest extends MvcIntegrationTests {
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
  public void article() throws Exception {

    ArticleVoter articleVoter = ArticleVoter.create(VoteState.UP,
        FlakeId.fromString("foo1"),
        user.getAccountId(),
        0,
        Instant.now());

    when(voteService.listArticleVoters(isA(ClientAppUserAccessToken.class),
        eq(asList(FlakeId.fromString("foo1"), FlakeId.fromString("foo2"))))).thenReturn(asList(
        articleVoter));

    oauthPerform(user,
        get("/v1/vote/article").param("article-id", "foo1,foo2")).andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].voteState", is("UP")));

  }

  @Test
  public void debate() throws Exception {
    DebateVoter debateVoter = DebateVoter.create(VoteState.DOWN,
        nextFlakeId(),
        FlakeId.fromString("bar123"),
        user.getAccountId(),
        0,
        Instant.now());

    when(voteService.listDebateVotersByIds(isA(ClientAppUserAccessToken.class),
        eq(asList(FlakeId.fromString("bar123"), FlakeId.fromString("bar456"))))).thenReturn(asList(
        debateVoter));

    oauthPerform(user,
        get("/v1/vote/debate").param("debate-id", "bar123,bar456")).andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].voteState", is("DOWN")))
        .andExpect(jsonPath("$.data[0].targetId", is("bar123")));
  }

  @Test
  public void debateOfArticles() throws Exception {
    DebateVoter debateVoter = DebateVoter.create(VoteState.DOWN,
        FlakeId.fromString("foo1"),
        FlakeId.fromString("bar123"),
        user.getAccountId(),
        0,
        Instant.now());

    when(voteService.listDebateVoters(isA(ClientAppUserAccessToken.class),
        eq(FlakeId.fromString("foo1")))).thenReturn(asList(debateVoter));

    oauthPerform(user, get("/v1/vote/debate/article/foo1")).andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].voteState", is("DOWN")))
        .andExpect(jsonPath("$.data[0].targetId", is("bar123")));

  }
}