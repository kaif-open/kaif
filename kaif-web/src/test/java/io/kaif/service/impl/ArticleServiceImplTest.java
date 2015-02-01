package io.kaif.service.impl;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.model.account.Account;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticleContentType;
import io.kaif.model.article.ArticleLinkType;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.DbIntegrationTests;

public class ArticleServiceImplTest extends DbIntegrationTests {

  @Autowired
  private ArticleServiceImpl service;

  @Test
  public void createExternalLink() throws Exception {
    ZoneInfo zoneInfo = savedZoneDefault("fun");
    Account account = savedAccountCitizen("citizen");
    Article created = service.createExternalLink(zoneInfo,
        account.getAccountId(),
        "title1",
        "http://foo.com");
    Article article = service.findArticle(created.getZone(), created.getArticleId().toString())
        .get();
    assertEquals("fun", article.getZone());
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
  }
}