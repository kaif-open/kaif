package io.kaif.service.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.model.account.Account;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticleContentType;
import io.kaif.model.article.ArticleLinkType;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.DbIntegrationTests;
import io.kaif.web.support.AccessDeniedException;

public class ArticleServiceImplTest extends DbIntegrationTests {

  @Autowired
  private ArticleServiceImpl service;

  @Test
  public void listNewArticles() throws Exception {
    ZoneInfo zoneInfo = savedZoneDefault("fun");
    Account account = savedAccountCitizen("citizen");
    Article a1 = service.createExternalLink(account.getAccountId(),
        zoneInfo.getZone(),
        "title1",
        "http://foo1.com");
    Article a2 = service.createExternalLink(account.getAccountId(),
        zoneInfo.getZone(),
        "title2",
        "http://foo2.com");
    Article a3 = service.createExternalLink(account.getAccountId(),
        zoneInfo.getZone(),
        "title2",
        "http://foo2.com");

    assertEquals(asList(a3, a2, a1), service.listLatestArticles(zoneInfo.getZone(), 0));
  }

  @Test
  public void createExternalLink() throws Exception {
    ZoneInfo zoneInfo = savedZoneDefault("fun");
    Account account = savedAccountCitizen("citizen");
    Article created = service.createExternalLink(account.getAccountId(),
        zoneInfo.getZone(),
        "title1",
        "http://foo.com");
    Article article = service.findArticle(created.getZone(), created.getArticleId()).get();
    assertEquals(Zone.valueOf("fun"), article.getZone());
    assertEquals("title1", article.getTitle());
    assertNull(article.getUrlName());
    assertNotNull(article.getCreateTime());
    assertEquals("http://foo.com", article.getContent());
    assertEquals(ArticleContentType.URL, article.getContentType());
    assertEquals(ArticleLinkType.EXTERNAL, article.getLinkType());
    assertEquals("citizen", article.getAuthorName());
    assertEquals(account.getAccountId(), article.getAuthorId());
    assertFalse(article.isDeleted());
    assertEquals(0, article.getUpVote());
    assertEquals(0, article.getDownVote());
    assertEquals(0, article.getDebateCount());
  }

  @Test
  public void createExternalLink_not_enough_authority() throws Exception {
    ZoneInfo zoneRequireCitizen = savedZoneDefault("fun");
    Account account = savedAccountTourist("notActivated");
    try {
      service.createExternalLink(account.getAccountId(),
          zoneRequireCitizen.getZone(),
          "title1",
          "http://foo.com");
      fail("AccessDeniedException expected");
    } catch (AccessDeniedException expected) {
    }
  }
}