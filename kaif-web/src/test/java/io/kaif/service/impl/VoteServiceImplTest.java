package io.kaif.service.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import io.kaif.model.account.Account;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticleDao;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateDao;
import io.kaif.model.vote.ArticleVoter;
import io.kaif.model.vote.DebateVoter;
import io.kaif.model.vote.VoteState;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.DbIntegrationTests;
import io.kaif.web.support.AccessDeniedException;

public class VoteServiceImplTest extends DbIntegrationTests {

  @Autowired
  private VoteServiceImpl service;

  @Autowired
  private ArticleDao articleDao;

  private ZoneInfo zoneInfo;
  private Article article;
  private Account voter;
  private Debate debate;
  @Autowired
  private DebateDao debateDao;

  @Before
  public void setUp() throws Exception {
    zoneInfo = savedZoneDefault("hacker");
    Account author = savedAccountCitizen("hc1");
    article = savedArticle(zoneInfo, author, "new cython 3");
    voter = savedAccountCitizen("vt");
    debate = savedDebate(article, "it is slow", null);
  }

  @Test
  public void vote_accessDenied() throws Exception {
    Account tourist = savedAccountTourist("no_permit");

    try {
      service.upVoteArticle(zoneInfo.getZone(), article.getArticleId(), tourist, 100);
      fail("AccessDeniedException expected");
    } catch (AccessDeniedException expected) {
    }
    try {
      service.cancelVoteArticle(zoneInfo.getZone(), article.getArticleId(), tourist);
      fail("AccessDeniedException expected");
    } catch (AccessDeniedException expected) {
    }
  }

  @Test
  public void upVoteArticle() throws Exception {
    service.upVoteArticle(zoneInfo.getZone(), article.getArticleId(), voter, 100);
    assertEquals(1,
        articleDao.findArticle(zoneInfo.getZone(), article.getArticleId()).get().getUpVote());

    List<ArticleVoter> votes = service.listArticleVotersInRage(voter,
        article.getArticleId(),
        article.getArticleId());
    assertEquals(1, votes.size());
    ArticleVoter vote = votes.get(0);
    assertEquals(voter.getAccountId(), vote.getVoterId());
    assertEquals(article.getArticleId(), vote.getArticleId());
    assertEquals(100, vote.getPreviousCount());
    assertFalse(vote.isCancel());
    assertNotNull(vote.getUpdateTime());
  }

  @Test
  public void upVoteArticle_not_allow_duplicate() throws Exception {
    service.upVoteArticle(zoneInfo.getZone(), article.getArticleId(), voter, 100);
    try {
      service.upVoteArticle(zoneInfo.getZone(), article.getArticleId(), voter, 101);
      fail("DuplicateKeyException expected");
    } catch (DuplicateKeyException expected) {
    }
  }

  @Test
  public void cancelVoteArticle_no_effect_if_not_exist() throws Exception {
    service.cancelVoteArticle(zoneInfo.getZone(), article.getArticleId(), voter);
    assertEquals(0,
        articleDao.findArticle(zoneInfo.getZone(), article.getArticleId()).get().getUpVote());

    List<ArticleVoter> votes = service.listArticleVotersInRage(voter,
        article.getArticleId(),
        article.getArticleId());
    assertEquals(0, votes.size());
  }

  @Test
  public void upVoteArticle_allow_on_canceled_vote() throws Exception {
    service.upVoteArticle(zoneInfo.getZone(), article.getArticleId(), voter, 100);
    service.cancelVoteArticle(zoneInfo.getZone(), article.getArticleId(), voter);
    service.upVoteArticle(zoneInfo.getZone(), article.getArticleId(), voter, 102);

    assertEquals(1,
        articleDao.findArticle(zoneInfo.getZone(), article.getArticleId()).get().getUpVote());

    List<ArticleVoter> votes = service.listArticleVotersInRage(voter,
        article.getArticleId(),
        article.getArticleId());
    assertEquals(1, votes.size());
    ArticleVoter vote = votes.get(0);
    assertFalse(vote.isCancel());
    assertEquals(102, vote.getPreviousCount());
  }

  @Test
  public void cancelVoteArticle() throws Exception {
    service.upVoteArticle(zoneInfo.getZone(), article.getArticleId(), voter, 100);
    service.cancelVoteArticle(zoneInfo.getZone(), article.getArticleId(), voter);
    assertEquals(0,
        articleDao.findArticle(zoneInfo.getZone(), article.getArticleId()).get().getUpVote());

    List<ArticleVoter> votes = service.listArticleVotersInRage(voter,
        article.getArticleId(),
        article.getArticleId());
    assertEquals(1, votes.size());
    ArticleVoter vote = votes.get(0);
    assertTrue(vote.isCancel());
    assertEquals(0, vote.getPreviousCount());
  }

  @Test
  public void cancelVoteArticle_twice() throws Exception {
    service.upVoteArticle(zoneInfo.getZone(), article.getArticleId(), voter, 100);
    service.cancelVoteArticle(zoneInfo.getZone(), article.getArticleId(), voter);
    service.cancelVoteArticle(zoneInfo.getZone(), article.getArticleId(), voter);
    assertEquals(0,
        articleDao.findArticle(zoneInfo.getZone(), article.getArticleId()).get().getUpVote());
  }

  @Test
  public void upVoteDebate_not_allow_duplicate() throws Exception {
    service.upVoteDebate(zoneInfo.getZone(),
        article.getArticleId(),
        debate.getDebateId(),
        voter,
        20,
        VoteState.EMPTY);
    try {
      service.upVoteDebate(zoneInfo.getZone(),
          article.getArticleId(),
          debate.getDebateId(),
          voter,
          20,
          VoteState.UP);
      fail("DuplicateKeyException expected");
    } catch (DuplicateKeyException expected) {
    }
  }

  @Test
  public void downVoteDebate_not_allow_duplicate() throws Exception {
    service.downVoteDebate(zoneInfo.getZone(),
        article.getArticleId(),
        debate.getDebateId(),
        voter,
        20,
        VoteState.EMPTY);
    try {
      service.downVoteDebate(zoneInfo.getZone(),
          article.getArticleId(),
          debate.getDebateId(),
          voter,
          20,
          VoteState.UP);
      fail("DuplicateKeyException expected");
    } catch (DuplicateKeyException expected) {
    }
  }

  @Test
  public void upVoteDebate() throws Exception {
    service.upVoteDebate(zoneInfo.getZone(),
        article.getArticleId(),
        debate.getDebateId(),
        voter,
        20,
        VoteState.EMPTY);

    assertDebateTotalVote(1, 0);

    List<DebateVoter> debateVoters = service.listDebateVoters(voter, article.getArticleId());
    assertEquals(1, debateVoters.size());
    DebateVoter debateVoter = debateVoters.get(0);
    assertEquals(voter.getAccountId(), debateVoter.getVoterId());
    assertEquals(article.getArticleId(), debateVoter.getArticleId());
    assertEquals(VoteState.UP, debateVoter.getVoteState());
    assertNotNull(debateVoter.getUpdateTime());
    assertEquals(20, debateVoter.getPreviousCount());
  }

  @Test
  public void cancelVoteDebate_ignore_not_exit() throws Exception {
    service.cancelVoteDebate(zoneInfo.getZone(),
        article.getArticleId(),
        debate.getDebateId(),
        voter,
        VoteState.EMPTY);

    assertDebateTotalVote(0, 0);
    assertEquals(0, service.listDebateVoters(voter, article.getArticleId()).size());
  }

  @Test
  public void cancelVoteDebate_ignore_wrong_previous_state() throws Exception {
    service.downVoteDebate(zoneInfo.getZone(),
        article.getArticleId(),
        debate.getDebateId(),
        voter,
        20,
        VoteState.EMPTY);

    //wrong previous state:
    service.cancelVoteDebate(zoneInfo.getZone(),
        article.getArticleId(),
        debate.getDebateId(),
        voter,
        VoteState.UP);

    assertDebateTotalVote(0, 1);

    DebateVoter debateVoter = service.listDebateVoters(voter, article.getArticleId()).get(0);
    assertEquals(VoteState.DOWN, debateVoter.getVoteState());
  }

  @Test
  public void cancelVoteDebate() throws Exception {
    service.upVoteDebate(zoneInfo.getZone(),
        article.getArticleId(),
        debate.getDebateId(),
        voter,
        20,
        VoteState.EMPTY);

    service.cancelVoteDebate(zoneInfo.getZone(),
        article.getArticleId(),
        debate.getDebateId(),
        voter,
        VoteState.UP);

    assertDebateTotalVote(0, 0);

    DebateVoter debateVoter = service.listDebateVoters(voter, article.getArticleId()).get(0);
    assertEquals(VoteState.EMPTY, debateVoter.getVoteState());
    assertEquals(0, debateVoter.getPreviousCount());
  }

  @Test
  public void downVoteDebate() throws Exception {
    service.downVoteDebate(zoneInfo.getZone(),
        article.getArticleId(),
        debate.getDebateId(),
        voter,
        20,
        VoteState.EMPTY);
    assertDebateTotalVote(0, 1);

    List<DebateVoter> debateVoters = service.listDebateVoters(voter, article.getArticleId());
    DebateVoter debateVoter = debateVoters.get(0);
    assertEquals(VoteState.DOWN, debateVoter.getVoteState());
    assertNotNull(debateVoter.getUpdateTime());
    assertEquals(20, debateVoter.getPreviousCount());
  }

  @Test
  public void debate_upVote_then_downVote() throws Exception {
    service.upVoteDebate(zoneInfo.getZone(),
        article.getArticleId(),
        debate.getDebateId(),
        voter,
        20,
        VoteState.EMPTY);

    service.downVoteDebate(zoneInfo.getZone(),
        article.getArticleId(),
        debate.getDebateId(),
        voter,
        49,
        VoteState.UP);
    assertDebateTotalVote(0, 1);

    List<DebateVoter> debateVoters = service.listDebateVoters(voter, article.getArticleId());
    DebateVoter debateVoter = debateVoters.get(0);
    assertEquals(VoteState.DOWN, debateVoter.getVoteState());
    assertEquals(49, debateVoter.getPreviousCount());
  }

  @Test
  public void debateVoteChain_up_down_up_cancel_down() throws Exception {
    assertDebateTotalVote(0, 0);

    service.upVoteDebate(zoneInfo.getZone(),
        article.getArticleId(),
        debate.getDebateId(),
        voter,
        20,
        VoteState.EMPTY);
    assertDebateTotalVote(1, 0);

    service.downVoteDebate(zoneInfo.getZone(),
        article.getArticleId(),
        debate.getDebateId(),
        voter,
        49,
        VoteState.UP);
    assertDebateTotalVote(0, 1);

    service.upVoteDebate(zoneInfo.getZone(),
        article.getArticleId(),
        debate.getDebateId(),
        voter,
        30,
        VoteState.DOWN);
    assertDebateTotalVote(1, 0);

    service.cancelVoteDebate(zoneInfo.getZone(),
        article.getArticleId(),
        debate.getDebateId(),
        voter,
        VoteState.UP);
    assertDebateTotalVote(0, 0);

    service.downVoteDebate(zoneInfo.getZone(),
        article.getArticleId(),
        debate.getDebateId(),
        voter,
        90,
        VoteState.EMPTY);
    assertDebateTotalVote(0, 1);

    DebateVoter debateVoter = service.listDebateVoters(voter, article.getArticleId()).get(0);
    assertEquals(VoteState.DOWN, debateVoter.getVoteState());
    assertEquals(90, debateVoter.getPreviousCount());
  }

  private void assertDebateTotalVote(long upVote, long downVote) {
    Debate changedDebate = debateDao.findDebate(article.getArticleId(), debate.getDebateId()).get();
    assertEquals(downVote, changedDebate.getDownVote());
    assertEquals(upVote, changedDebate.getUpVote());
  }
}