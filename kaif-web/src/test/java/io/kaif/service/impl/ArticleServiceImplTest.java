package io.kaif.service.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticleContentType;
import io.kaif.model.article.ArticleLinkType;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateContentType;
import io.kaif.model.debate.DebateDao;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.DbIntegrationTests;
import io.kaif.web.support.AccessDeniedException;

public class ArticleServiceImplTest extends DbIntegrationTests {

  @Autowired
  private ArticleServiceImpl service;

  @Autowired
  private DebateDao debateDao;

  private ZoneInfo zoneInfo;
  private Article article;
  private Account citizen;

  @Before
  public void setUp() throws Exception {
    zoneInfo = savedZoneDefault("pic");
    citizen = savedAccountCitizen("citizen1");
    article = savedArticle(zoneInfo, citizen, "art-1");
  }

  @Test
  public void debate() throws Exception {
    Account debater = savedAccountCitizen("debater1");
    Debate created = service.debate(zoneInfo.getZone(),
        article.getArticleId(),
        Debate.NO_PARENT,
        debater.getAccountId(),
        "pixel art is better");

    Debate debate = debateDao.findDebate(article.getArticleId(), created.getDebateId()).get();
    assertEquals(DebateContentType.MARK_DOWN, debate.getContentType());
    assertEquals("debater1", debate.getDebaterName());
    assertEquals(debater.getAccountId(), debate.getDebaterId());
    assertFalse(debate.hasParent());
    assertEquals(1, debate.getLevel());
    assertEquals("pixel art is better", debate.getContent());
    assertEquals(0L, debate.getDownVote());
    assertEquals(0L, debate.getUpVote());
    assertNotNull(debate.getCreateTime());
    assertNotNull(debate.getLastUpdateTime());
  }

  @Test
  public void debate_max_level() throws Exception {
    Account debater = savedAccountCitizen("debater1");
    FlakeId parentId = Debate.NO_PARENT;
    for (int i = 0; i < 10; i++) {
      Debate next = service.debate(zoneInfo.getZone(),
          article.getArticleId(),
          parentId,
          debater.getAccountId(),
          "nested");
      parentId = next.getDebateId();
    }

    try {
      service.debate(zoneInfo.getZone(),
          article.getArticleId(),
          parentId,
          debater.getAccountId(),
          "failed");
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void debate_reply() throws Exception {
    Account debater = savedAccountCitizen("debater1");
    Debate l1 = service.debate(zoneInfo.getZone(),
        article.getArticleId(),
        Debate.NO_PARENT,
        debater.getAccountId(),
        "pixel art is better");
    Debate l2 = service.debate(zoneInfo.getZone(),
        article.getArticleId(),
        l1.getDebateId(),
        debater.getAccountId(),
        "i think so");
    assertEquals(2, l2.getLevel());
    assertTrue(l2.hasParent());
    assertTrue(l2.isParent(l1));
    assertFalse(l1.isParent(l2));

    Debate l3 = service.debate(zoneInfo.getZone(),
        article.getArticleId(),
        l2.getDebateId(),
        debater.getAccountId(),
        "no no no");

    assertEquals(3, l3.getLevel());
    assertTrue(l3.hasParent());
    assertTrue(l3.isParent(l2));
    assertFalse(l2.isParent(l3));
  }

  @Test
  public void debate_not_enough_authority() throws Exception {
    ZoneInfo zoneRequireCitizen = savedZoneDefault("fun");
    Article article = savedArticle(zoneRequireCitizen, citizen, "fun-no1");
    Account tourist = savedAccountTourist("notActivated");
    try {
      service.debate(zoneRequireCitizen.getZone(),
          article.getArticleId(),
          Debate.NO_PARENT,
          tourist.getAccountId(),
          "pixel art is better");
      fail("AccessDeniedException expected");
    } catch (AccessDeniedException expected) {
    }
  }

  @Test
  public void listNewArticles() throws Exception {
    Account author = savedAccountCitizen("citizen");
    ZoneInfo fooZone = savedZoneDefault("foo");
    Article a1 = service.createExternalLink(author.getAccountId(),
        fooZone.getZone(),
        "title1",
        "http://foo1.com");
    Article a2 = service.createExternalLink(author.getAccountId(),
        fooZone.getZone(),
        "title2",
        "http://foo2.com");
    Article a3 = service.createExternalLink(author.getAccountId(),
        fooZone.getZone(),
        "title2",
        "http://foo2.com");

    assertEquals(asList(a3, a2, a1), service.listLatestArticles(fooZone.getZone(), 0));
  }

  @Test
  public void createExternalLink() throws Exception {
    Article created = service.createExternalLink(citizen.getAccountId(),
        zoneInfo.getZone(),
        "title1",
        "http://foo.com");
    Article article = service.findArticle(created.getZone(), created.getArticleId()).get();
    assertEquals(zoneInfo.getZone(), article.getZone());
    assertEquals("title1", article.getTitle());
    assertNull(article.getUrlName());
    assertNotNull(article.getCreateTime());
    assertEquals("http://foo.com", article.getContent());
    assertEquals(ArticleContentType.URL, article.getContentType());
    assertEquals(ArticleLinkType.EXTERNAL, article.getLinkType());
    assertEquals(citizen.getUsername(), article.getAuthorName());
    assertEquals(citizen.getAccountId(), article.getAuthorId());
    assertFalse(article.isDeleted());
    assertEquals(0, article.getUpVote());
    assertEquals(0, article.getDownVote());
    assertEquals(0, article.getDebateCount());
  }

  @Test
  public void createExternalLink_not_enough_authority() throws Exception {
    ZoneInfo zoneRequireCitizen = savedZoneDefault("fun");
    Account tourist = savedAccountTourist("notActivated");
    try {
      service.createExternalLink(tourist.getAccountId(),
          zoneRequireCitizen.getZone(),
          "title1",
          "http://foo.com");
      fail("AccessDeniedException expected");
    } catch (AccessDeniedException expected) {
    }
  }
}