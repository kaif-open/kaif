package io.kaif.service.impl;

import static io.kaif.model.vote.VoteState.DOWN;
import static io.kaif.model.vote.VoteState.EMPTY;
import static io.kaif.model.vote.VoteState.UP;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticleDao;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateDao;
import io.kaif.model.vote.ArticleVoter;
import io.kaif.model.vote.DebateVoter;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.DbIntegrationTests;
import io.kaif.web.support.AccessDeniedException;

public class VoteServiceImplTest extends DbIntegrationTests {

  @Autowired
  private VoteServiceImpl service;

  @Autowired
  private ArticleDao articleDao;

  @Autowired
  private DebateDao debateDao;
  private Zone zone;
  private FlakeId articleId;
  private FlakeId debateId;
  private Account voter;

  @Before
  public void setUp() throws Exception {
    ZoneInfo zoneInfo = savedZoneDefault("hacker");
    Account author = savedAccountCitizen("hc1");
    Article article = savedArticle(zoneInfo, author, "new cython 3");
    Debate debate = savedDebate(article, "it is slow", null);

    voter = savedAccountCitizen("vt");
    zone = zoneInfo.getZone();
    articleId = article.getArticleId();
    debateId = debate.getDebateId();
  }

  @Test
  public void vote_accessDenied() throws Exception {
    Account tourist = savedAccountTourist("no_permit");

    try {
      service.voteArticle(UP, zone, articleId, tourist, EMPTY, 100);
      fail("AccessDeniedException expected");
    } catch (AccessDeniedException expected) {
    }
    try {
      service.voteDebate(UP, zone, articleId, debateId, tourist, EMPTY, 100);
      fail("AccessDeniedException expected");
    } catch (AccessDeniedException expected) {
    }
  }

  @Test
  public void articleNotAllowDownVote() throws Exception {
    try {
      service.voteArticle(DOWN, zone, articleId, voter, EMPTY, 100);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void upVoteArticle() throws Exception {
    service.voteArticle(UP, zone, articleId, voter, EMPTY, 100);

    assertArticleTotalVote(1);

    List<ArticleVoter> votes = service.listArticleVotersInRage(voter, articleId, articleId);
    assertEquals(1, votes.size());
    ArticleVoter vote = votes.get(0);
    assertEquals(voter.getAccountId(), vote.getVoterId());
    assertEquals(articleId, vote.getArticleId());
    assertEquals(100, vote.getPreviousCount());
    assertEquals(UP, vote.getVoteState());
    assertNotNull(vote.getUpdateTime());
  }

  @Test
  public void upVoteArticle_ignore_duplicate() throws Exception {
    service.voteArticle(UP, zone, articleId, voter, EMPTY, 100);
    service.voteArticle(UP, zone, articleId, voter, UP, 100);
    assertArticleTotalVote(1);
  }

  @Test
  public void cancelVoteArticle_no_effect_if_not_exist() throws Exception {
    service.voteArticle(EMPTY, zone, articleId, voter, EMPTY, 10);
    assertArticleTotalVote(0);

    List<ArticleVoter> votes = service.listArticleVotersInRage(voter, articleId, articleId);
    assertEquals(EMPTY, votes.get(0).getVoteState());
  }

  @Test
  public void upVoteArticle_allow_on_canceled_vote() throws Exception {
    service.voteArticle(UP, zone, articleId, voter, EMPTY, 100);
    service.voteArticle(EMPTY, zone, articleId, voter, UP, 20);
    service.voteArticle(UP, zone, articleId, voter, EMPTY, 102);

    assertArticleTotalVote(1);

    List<ArticleVoter> votes = service.listArticleVotersInRage(voter, articleId, articleId);
    assertEquals(1, votes.size());
    ArticleVoter vote = votes.get(0);
    assertEquals(UP, vote.getVoteState());
    assertEquals(102, vote.getPreviousCount());
  }

  @Test
  public void cancelVoteArticle() throws Exception {
    service.voteArticle(UP, zone, articleId, voter, EMPTY, 100);
    service.voteArticle(EMPTY, zone, articleId, voter, UP, 0);
    assertArticleTotalVote(0);

    List<ArticleVoter> votes = service.listArticleVotersInRage(voter, articleId, articleId);
    assertEquals(1, votes.size());
    ArticleVoter vote = votes.get(0);
    assertEquals(EMPTY, vote.getVoteState());
    assertEquals(0, vote.getPreviousCount());
  }

  @Test
  public void cancelVoteArticle_twice() throws Exception {
    service.voteArticle(UP, zone, articleId, voter, EMPTY, 100);
    service.voteArticle(EMPTY, zone, articleId, voter, UP, 100);
    service.voteArticle(EMPTY, zone, articleId, voter, EMPTY, 100);
    assertEquals(0, articleDao.findArticle(zone, articleId).get().getUpVote());
  }

  @Test
  public void voteDebate_ignore_duplicate() throws Exception {
    service.voteDebate(UP, zone, articleId, debateId, voter, EMPTY, 20);
    assertDebateTotalVote(1, 0);
    service.voteDebate(UP, zone, articleId, debateId, voter, UP, 20);
    assertDebateTotalVote(1, 0);
    assertEquals(UP, service.listDebateVoters(voter, articleId).get(0).getVoteState());
  }

  @Test
  public void upVoteDebate() throws Exception {
    service.voteDebate(UP, zone, articleId, debateId, voter, EMPTY, 20);

    assertDebateTotalVote(1, 0);

    List<DebateVoter> debateVoters = service.listDebateVoters(voter, articleId);
    assertEquals(1, debateVoters.size());
    DebateVoter debateVoter = debateVoters.get(0);
    assertEquals(voter.getAccountId(), debateVoter.getVoterId());
    assertEquals(articleId, debateVoter.getArticleId());
    assertEquals(UP, debateVoter.getVoteState());
    assertNotNull(debateVoter.getUpdateTime());
    assertEquals(20, debateVoter.getPreviousCount());
  }

  @Test
  public void cancelVoteDebate_no_change_if_not_exist() throws Exception {
    service.voteDebate(EMPTY, zone, articleId, debateId, voter, EMPTY, 0);

    assertDebateTotalVote(0, 0);
    assertEquals(EMPTY, service.listDebateVoters(voter, articleId).get(0).getVoteState());
  }

  @Test
  public void voteDebate_not_allow_wrong_previous_state() throws Exception {
    service.voteDebate(DOWN, zone, articleId, debateId, voter, EMPTY, 20);

    //wrong previous state:
    try {
      service.voteDebate(EMPTY, zone, articleId, debateId, voter, UP, 0);
      fail("DuplicateKeyException expected");
    } catch (DuplicateKeyException expected) {
    }

  }

  @Test
  public void voteDebate_up_then_cancel() throws Exception {
    service.voteDebate(UP, zone, articleId, debateId, voter, EMPTY, 20);

    service.voteDebate(EMPTY, zone, articleId, debateId, voter, UP, 0);

    assertDebateTotalVote(0, 0);

    DebateVoter debateVoter = service.listDebateVoters(voter, articleId).get(0);
    assertEquals(EMPTY, debateVoter.getVoteState());
    assertEquals(0, debateVoter.getPreviousCount());
  }

  @Test
  public void downVoteDebate() throws Exception {
    service.voteDebate(DOWN, zone, articleId, debateId, voter, EMPTY, 20);
    assertDebateTotalVote(0, 1);

    List<DebateVoter> debateVoters = service.listDebateVoters(voter, articleId);
    DebateVoter debateVoter = debateVoters.get(0);
    assertEquals(DOWN, debateVoter.getVoteState());
    assertNotNull(debateVoter.getUpdateTime());
    assertEquals(20, debateVoter.getPreviousCount());
  }

  @Test
  public void debate_upVote_then_downVote() throws Exception {
    service.voteDebate(UP, zone, articleId, debateId, voter, EMPTY, 20);

    service.voteDebate(DOWN, zone, articleId, debateId, voter, UP, 49);
    assertDebateTotalVote(0, 1);

    List<DebateVoter> debateVoters = service.listDebateVoters(voter, articleId);
    DebateVoter debateVoter = debateVoters.get(0);
    assertEquals(DOWN, debateVoter.getVoteState());
    assertEquals(49, debateVoter.getPreviousCount());
  }

  @Test
  public void debateVoteChain_up_down_up_cancel_down() throws Exception {
    assertDebateTotalVote(0, 0);

    service.voteDebate(UP, zone, articleId, debateId, voter, EMPTY, 20);
    assertDebateTotalVote(1, 0);

    service.voteDebate(DOWN, zone, articleId, debateId, voter, UP, 49);
    assertDebateTotalVote(0, 1);

    service.voteDebate(UP, zone, articleId, debateId, voter, DOWN, 30);
    assertDebateTotalVote(1, 0);

    service.voteDebate(EMPTY, zone, articleId, debateId, voter, UP, 0);
    assertDebateTotalVote(0, 0);

    service.voteDebate(DOWN, zone, articleId, debateId, voter, EMPTY, 90);
    assertDebateTotalVote(0, 1);

    DebateVoter debateVoter = service.listDebateVoters(voter, articleId).get(0);
    assertEquals(DOWN, debateVoter.getVoteState());
    assertEquals(90, debateVoter.getPreviousCount());
  }

  private void assertDebateTotalVote(long upVote, long downVote) {
    Debate changedDebate = debateDao.findDebate(articleId, debateId).get();
    assertEquals(downVote, changedDebate.getDownVote());
    assertEquals(upVote, changedDebate.getUpVote());
  }

  private void assertArticleTotalVote(long upVote) {
    Article changed = articleDao.findArticle(zone, articleId).get();
    assertEquals(upVote, changed.getUpVote());
  }
}