package io.kaif.model.article;

import static org.junit.Assert.*;

import java.time.Instant;

import org.junit.Test;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.zone.Zone;
import io.kaif.test.ModelFixture;

public class ArticleTest implements ModelFixture {

  FlakeId articleId = FlakeId.valueOf(123);
  Zone zone = Zone.valueOf("abc");
  Account account = accountCitizen("foo");
  Instant now = Instant.now();

  @Test
  public void linkHintForExternal() throws Exception {

    Article externalLink = Article.createExternalLink(zone,
        articleId,
        account,
        "title",
        "http://foo.com",
        now);

    assertEquals("foo.com", externalLink.getLinkHint());

    externalLink = Article.createExternalLink(zone,
        articleId,
        account,
        "title",
        "httPS://bar.com/xyz.123/?999",
        now);

    assertEquals("bar.com", externalLink.getLinkHint());
  }

  @Test
  public void linkHintForExternal_malformed() throws Exception {

    Article externalLink = Article.createExternalLink(zone,
        articleId,
        account,
        "title",
        "foo.com",
        now);

    assertEquals("--bad--", externalLink.getLinkHint());
    externalLink = Article.createExternalLink(zone, articleId, account, "title", null, now);
    assertEquals("--bad--", externalLink.getLinkHint());
  }
}