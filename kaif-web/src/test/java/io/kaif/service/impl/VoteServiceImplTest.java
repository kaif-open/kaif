package io.kaif.service.impl;

import static io.kaif.model.vote.VoteState.DOWN;
import static io.kaif.model.vote.VoteState.EMPTY;
import static io.kaif.model.vote.VoteState.UP;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import com.google.common.collect.Sets;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.account.AccountDao;
import io.kaif.model.account.AccountStats;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticleDao;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateDao;
import io.kaif.model.vote.ArticleVoter;
import io.kaif.model.vote.DebateVoter;
import io.kaif.model.vote.HonorRoll;
import io.kaif.model.vote.HonorRollDao;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.DbIntegrationTests;
import io.kaif.web.support.AccessDeniedException;

public class VoteServiceImplTest extends DbIntegrationTests {

  @Autowired
  private VoteServiceImpl service;

  @Autowired
  private AccountDao accountDao;

  @Autowired
  private ArticleDao articleDao;

  @Autowired
  private DebateDao debateDao;

  @Autowired
  private HonorRollDao honorRollDao;

  private Zone zone;
  private FlakeId articleId;
  private FlakeId debateId;
  private Account voter;
  private Account debater;
  private Article article;
  private ZoneInfo zoneInfo;
  private Account author;

  @Before
  public void setUp() throws Exception {
    zoneInfo = savedZoneDefault("hacker");
    author = savedAccountCitizen("hc1");
    article = savedArticle(zoneInfo, author, "new cython 3");
    Debate debate = savedDebate(article, "it is slow", null);

    voter = savedAccountCitizen("vt");
    zone = zoneInfo.getZone();
    articleId = article.getArticleId();
    debateId = debate.getDebateId();
    debater = accountDao.findById(debate.getDebaterId()).get();
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
  public void listArticleVoters() throws Exception {
    assertEquals(0, service.listArticleVoters(voter, Collections.emptyList()).size());

    service.voteArticle(UP, zone, articleId, voter, EMPTY, 100);
    Article a2 = savedArticle(zoneInfo, savedAccountCitizen("author2"), "title vote");
    service.voteArticle(UP, zone, a2.getArticleId(), voter, EMPTY, 200);

    Set<FlakeId> actual = service.listArticleVoters(voter, asList(articleId, a2.getArticleId()))
        .stream()
        .map(ArticleVoter::getArticleId)
        .collect(Collectors.toSet());
    assertEquals(Sets.newHashSet(articleId, a2.getArticleId()), actual);
  }

  @Test
  public void upVoteArticle() throws Exception {
    service.voteArticle(UP, zone, articleId, voter, EMPTY, 100);

    assertArticleTotalVote(1);
    assertArticleRotateVoteStats(1);

    List<ArticleVoter> votes = service.listArticleVoters(voter, asList(articleId));
    assertEquals(1, votes.size());
    ArticleVoter vote = votes.get(0);
    assertEquals(voter.getAccountId(), vote.getVoterId());
    assertEquals(articleId, vote.getArticleId());
    assertEquals(100, vote.getPreviousCount());
    assertEquals(UP, vote.getVoteState());
    assertNotNull(vote.getUpdateTime());

  }

  @Test
  public void listUpVotedArticles() throws Exception {
    assertEquals(0, service.listUpVotedArticles(voter, null).size());
    savedArticle(zoneInfo, savedAccountCitizen("other2"), "not voted");
    Article a3 = savedArticle(zoneInfo, savedAccountCitizen("other3"), "title 3");
    service.voteArticle(UP, zone, a3.getArticleId(), voter, EMPTY, 100);
    service.voteArticle(UP, zone, articleId, voter, EMPTY, 100);

    assertEquals(asList(a3, article), service.listUpVotedArticles(voter, null));
    assertEquals(asList(article), service.listUpVotedArticles(voter, a3.getArticleId()));
  }

  @Test
  public void upVoteArticle_ignore_duplicate() throws Exception {
    service.voteArticle(UP, zone, articleId, voter, EMPTY, 100);
    service.voteArticle(UP, zone, articleId, voter, UP, 100);
    assertArticleTotalVote(1);
    assertArticleRotateVoteStats(1);
  }

  @Test
  public void cancelVoteArticle_no_effect_if_not_exist() throws Exception {
    service.voteArticle(EMPTY, zone, articleId, voter, EMPTY, 10);
    assertArticleTotalVote(0);
    assertArticleRotateVoteStats(0);

    List<ArticleVoter> votes = service.listArticleVoters(voter, asList(articleId));
    assertEquals(EMPTY, votes.get(0).getVoteState());
  }

  @Test
  public void upVoteArticle_allow_on_canceled_vote() throws Exception {
    service.voteArticle(UP, zone, articleId, voter, EMPTY, 100);
    service.voteArticle(EMPTY, zone, articleId, voter, UP, 20);
    service.voteArticle(UP, zone, articleId, voter, EMPTY, 102);

    assertArticleTotalVote(1);
    assertArticleRotateVoteStats(1);

    List<ArticleVoter> votes = service.listArticleVoters(voter, asList(articleId));
    assertEquals(1, votes.size());
    ArticleVoter vote = votes.get(0);
    assertEquals(UP, vote.getVoteState());
    assertEquals(102, vote.getPreviousCount());
  }

  @Test
  public void touristVoteDoNotCountInTotalVote() throws Exception {
    ZoneInfo zoneTourist = savedZoneTourist("test");
    Zone z = zoneTourist.getZone();
    Account tourist = savedAccountTourist("guest-a");
    Article testArticle = savedArticle(zoneTourist, tourist, "test article");
    Debate testDebate = savedDebate(testArticle, "foo", null);
    service.voteDebate(UP,
        z,
        testArticle.getArticleId(),
        testDebate.getDebateId(),
        tourist,
        EMPTY,
        100);

    Debate changedDebate = debateDao.findDebate(testDebate.getDebateId()).get();
    assertEquals(0, changedDebate.getDownVote());
    assertEquals(1, changedDebate.getUpVote());

    AccountStats stats = accountDao.loadStats(debater.getUsername());
    assertEquals(0, stats.getDebateDownVoted());
    assertEquals(0, stats.getDebateUpVoted());
  }

  @Test
  public void articleSelfVoteDoNotCountInRotateScore() throws Exception {
    service.voteArticle(UP, zone, articleId, author, EMPTY, 100);
    assertArticleTotalVote(1);
    assertEquals(Optional.empty(),
        honorRollDao.findHonorRoll(author.getAccountId(),
            zone,
            Instant.ofEpochMilli(article.getArticleId().epochMilli())));
  }

  @Test
  public void debateSelfVoteDoNotCountInTotalVote() throws Exception {
    service.voteDebate(UP, zone, articleId, debateId, debater, EMPTY, 20);

    AccountStats stats = accountDao.loadStats(debater.getUsername());
    assertEquals(0, stats.getDebateDownVoted());
    assertEquals(0, stats.getDebateUpVoted());

    assertEquals(Optional.empty(),
        honorRollDao.findHonorRoll(debater.getAccountId(),
            zone,
            Instant.ofEpochMilli(debateId.epochMilli())));
  }

  @Test
  public void cancelVoteArticle() throws Exception {
    service.voteArticle(UP, zone, articleId, voter, EMPTY, 100);
    service.voteArticle(EMPTY, zone, articleId, voter, UP, 0);
    assertArticleTotalVote(0);

    List<ArticleVoter> votes = service.listArticleVoters(voter, asList(articleId));
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
    assertEquals(0, articleDao.findArticleWithoutCache(articleId).get().getUpVote());
  }

  @Test
  public void voteDebate_ignore_duplicate() throws Exception {
    service.voteDebate(UP, zone, articleId, debateId, voter, EMPTY, 20);
    assertDebateTotalVote(1, 0);
    assertDebateRotateVoteStats(1, 0);
    service.voteDebate(UP, zone, articleId, debateId, voter, UP, 20);
    assertDebateTotalVote(1, 0);
    assertDebateRotateVoteStats(1, 0);
    assertEquals(UP, service.listDebateVoters(voter, articleId).get(0).getVoteState());
  }

  @Test
  public void listDebateVotersByIds() throws Exception {
    assertEquals(0, service.listDebateVotersByIds(voter, Collections.emptyList()).size());

    service.voteDebate(UP, zone, articleId, debateId, voter, EMPTY, 20);
    Article a2 = savedArticle(zoneInfo, author, "another article");
    Debate d2 = savedDebate(a2, "foo", null);
    service.voteDebate(UP, zone, a2.getArticleId(), d2.getDebateId(), voter, EMPTY, 20);
    List<DebateVoter> debateVoters = service.listDebateVotersByIds(voter,
        asList(debateId, d2.getDebateId()));
    assertEquals(2, debateVoters.size());
    assertEquals(debateId, debateVoters.get(0).getDebateId());
    assertEquals(d2.getDebateId(), debateVoters.get(1).getDebateId());
  }

  @Test
  public void upVoteDebate() throws Exception {
    service.voteDebate(UP, zone, articleId, debateId, voter, EMPTY, 20);

    assertDebateTotalVote(1, 0);
    assertDebateRotateVoteStats(1, 0);

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
    assertDebateRotateVoteStats(0, 0);

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
    assertDebateRotateVoteStats(0, 0);

    DebateVoter debateVoter = service.listDebateVoters(voter, articleId).get(0);
    assertEquals(EMPTY, debateVoter.getVoteState());
    assertEquals(0, debateVoter.getPreviousCount());
  }

  @Test
  public void downVoteDebate() throws Exception {
    service.voteDebate(DOWN, zone, articleId, debateId, voter, EMPTY, 20);
    assertDebateTotalVote(0, 1);
    assertDebateRotateVoteStats(0, 1);

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
    assertDebateRotateVoteStats(0, 1);

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
    assertDebateRotateVoteStats(1, 0);

    service.voteDebate(DOWN, zone, articleId, debateId, voter, UP, 49);
    assertDebateTotalVote(0, 1);
    assertDebateRotateVoteStats(0, 1);

    service.voteDebate(UP, zone, articleId, debateId, voter, DOWN, 30);
    assertDebateTotalVote(1, 0);
    assertDebateRotateVoteStats(1, 0);

    service.voteDebate(EMPTY, zone, articleId, debateId, voter, UP, 0);
    assertDebateTotalVote(0, 0);
    assertDebateRotateVoteStats(0, 0);

    service.voteDebate(DOWN, zone, articleId, debateId, voter, EMPTY, 90);
    assertDebateTotalVote(0, 1);
    assertDebateRotateVoteStats(0, 1);

    DebateVoter debateVoter = service.listDebateVoters(voter, articleId).get(0);
    assertEquals(DOWN, debateVoter.getVoteState());
    assertEquals(90, debateVoter.getPreviousCount());
  }

  private void assertDebateTotalVote(long upVote, long downVote) {
    Debate changedDebate = debateDao.findDebate(debateId).get();
    assertEquals(downVote, changedDebate.getDownVote());
    assertEquals(upVote, changedDebate.getUpVote());

    AccountStats stats = accountDao.loadStats(debater.getUsername());
    assertEquals(downVote, stats.getDebateDownVoted());
    assertEquals(upVote, stats.getDebateUpVoted());
  }

  private void assertArticleTotalVote(long upVote) {
    Article changed = articleDao.findArticleWithoutCache(articleId).get();
    assertEquals(upVote, changed.getUpVote());
  }

  private void assertDebateRotateVoteStats(long upVoteDebate, long downVoteDebate) {
    HonorRoll stats = honorRollDao.findHonorRoll(debater.getAccountId(),
        zone,
        Instant.ofEpochMilli(debateId.epochMilli())).get();
    assertEquals(upVoteDebate, stats.getDebateUpVoted());
    assertEquals(downVoteDebate, stats.getDebateDownVoted());
  }

  private void assertArticleRotateVoteStats(long upVoteArticle) {
    HonorRoll stats = honorRollDao.findHonorRoll(author.getAccountId(),
        zone,
        Instant.ofEpochMilli(article.getArticleId().epochMilli())).get();
    assertEquals(upVoteArticle, stats.getArticleUpVoted());
  }
}