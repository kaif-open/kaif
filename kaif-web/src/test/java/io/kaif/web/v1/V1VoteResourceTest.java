package io.kaif.web.v1;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.dao.DuplicateKeyException;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.model.vote.ArticleVoter;
import io.kaif.model.vote.DebateVoter;
import io.kaif.model.vote.VoteState;
import io.kaif.test.MvcIntegrationTests;

public class V1VoteResourceTest extends MvcIntegrationTests {
  private Account user;

  @Before
  public void setUp() throws Exception {
    user = accountCitizen("user1");
  }

  @Test
  public void article() throws Exception {
    ArticleVoter articleVoter = articleVoter(VoteState.UP, "foo1");
    when(voteService.listArticleVoters(isA(ClientAppUserAccessToken.class),
        eq(asList(FlakeId.fromString("foo1"), FlakeId.fromString("foo2"))))).thenReturn(asList(
        articleVoter));

    oauthPerform(user,
        get("/v1/vote/article").param("article-id", "foo1,foo2")).andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].voteState", is("UP")));
  }

  @Test
  public void debate() throws Exception {
    DebateVoter debateVoter = debateVoter(VoteState.DOWN, "foo1", "bar123");
    when(voteService.listDebateVotersByIds(isA(ClientAppUserAccessToken.class),
        eq(asList(FlakeId.fromString("bar123"), FlakeId.fromString("bar456"))))).thenReturn(asList(
        debateVoter));

    oauthPerform(user,
        get("/v1/vote/debate").param("debate-id", "bar123,bar456")).andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].voteState", is("DOWN")))
        .andExpect(jsonPath("$.data[0].targetId", is("bar123")));
  }

  @Test
  public void voteDebate() throws Exception {
    DebateVoter debateVoter = debateVoter(VoteState.DOWN, "foo1", "bar123");
    when(voteService.listDebateVotersByIds(isA(ClientAppUserAccessToken.class),
        eq(asList(FlakeId.fromString("bar123"))))).thenReturn(asList(debateVoter));

    oauthPerform(user, post("/v1/vote/debate").content(q("{'voteState':'UP','debateId':'bar123'}")))
        .andExpect(status().isOk());

    verify(voteService).voteDebate(eq(VoteState.UP),
        eq(FlakeId.fromString("bar123")),
        isA(ClientAppUserAccessToken.class),
        eq(VoteState.DOWN),
        eq(0L));
  }

  @Test
  public void voteArticle() throws Exception {
    ArticleVoter articleVoter = articleVoter(VoteState.UP, "foo1");
    when(voteService.listArticleVoters(isA(ClientAppUserAccessToken.class),
        eq(asList(FlakeId.fromString("foo1"))))).thenReturn(asList(articleVoter));

    oauthPerform(user,
        post("/v1/vote/article").content(q("{'voteState':'EMPTY','articleId':'foo1'}"))).andExpect(
        status().isOk());

    verify(voteService).voteArticle(eq(VoteState.EMPTY),
        eq(FlakeId.fromString("foo1")),
        isA(ClientAppUserAccessToken.class),
        eq(VoteState.UP),
        eq(0L));
  }

  @Test
  public void ignoreDuplicateKeyException() throws Exception {
    ArticleVoter articleVoter = articleVoter(VoteState.UP, "foo1");
    when(voteService.listArticleVoters(isA(ClientAppUserAccessToken.class),
        eq(asList(FlakeId.fromString("foo1"))))).thenReturn(asList(articleVoter));

    Mockito.doThrow(new DuplicateKeyException("fake"))
        .when(voteService)
        .voteArticle(eq(VoteState.EMPTY),
            eq(FlakeId.fromString("foo1")),
            isA(ClientAppUserAccessToken.class),
            eq(VoteState.UP),
            eq(0L));

    oauthPerform(user,
        post("/v1/vote/article").content(q("{'voteState':'EMPTY','articleId':'foo1'}"))).andExpect(
        status().isOk());
  }

  private ArticleVoter articleVoter(VoteState voteState, String articleId) {
    return ArticleVoter.create(voteState,
        FlakeId.fromString(articleId),
        user.getAccountId(),
        0,
        Instant.now());
  }

  @Test
  public void debateOfArticles() throws Exception {
    DebateVoter debateVoter = debateVoter(VoteState.DOWN, "foo1", "bar123");
    when(voteService.listDebateVoters(isA(ClientAppUserAccessToken.class),
        eq(FlakeId.fromString("foo1")))).thenReturn(asList(debateVoter));

    oauthPerform(user, get("/v1/vote/debate/article/foo1")).andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].voteState", is("DOWN")))
        .andExpect(jsonPath("$.data[0].targetId", is("bar123")));

  }

  private DebateVoter debateVoter(VoteState voteState, String articleId, String debateId) {
    return DebateVoter.create(voteState,
        FlakeId.fromString(articleId),
        FlakeId.fromString(debateId),
        user.getAccountId(),
        0,
        Instant.now());
  }
}