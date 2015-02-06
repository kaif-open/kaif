package io.kaif.service.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

  @Before
  public void setUp() throws Exception {
    zoneInfo = savedZoneDefault("hacker");
    Account author = savedAccountCitizen("hc1");
    article = savedArticle(zoneInfo, author, "new cython 3");
  }

  @Test
  public void upVoteArticle() throws Exception {

    Account voter = savedAccountCitizen("vt");
    service.upVoteArticle(zoneInfo.getZone(), article.getArticleId(), voter.getAccountId(), 100);
    assertEquals(1,
        articleDao.findArticle(zoneInfo.getZone(), article.getArticleId()).get().getUpVote());

    List<ArticleVoter> votes = service.listArticleVotersInRage(voter.getAccountId(),
        article.getArticleId(),
        article.getArticleId());
    assertEquals(1, votes.size());
    ArticleVoter vote = votes.get(0);
    assertEquals(voter.getAccountId(), vote.getVoterId());
    assertEquals(article.getArticleId(), vote.getArticleId());
    assertEquals(100, vote.getPreviousCount());
    assertNotNull(vote.getUpdateTime());
  }
}