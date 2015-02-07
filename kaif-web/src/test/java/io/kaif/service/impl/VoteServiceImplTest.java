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
import io.kaif.model.vote.ArticleVoter;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.DbIntegrationTests;

public class VoteServiceImplTest extends DbIntegrationTests {

  @Autowired
  private VoteServiceImpl service;

  @Autowired
  private ArticleDao articleDao;

  private ZoneInfo zoneInfo;
  private Article article;
  private Account voter;

  @Before
  public void setUp() throws Exception {
    zoneInfo = savedZoneDefault("hacker");
    Account author = savedAccountCitizen("hc1");
    article = savedArticle(zoneInfo, author, "new cython 3");
    voter = savedAccountCitizen("vt");
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
}