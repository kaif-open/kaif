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
  String zoneAlias = "abc-alias";
  Account account = accountCitizen("foo");
  Instant now = Instant.now();

  @Test
  public void createExternalLink_escape() throws Exception {
    Article article = Article.createExternalLink(zone,
        zoneAlias,
        articleId,
        account,
        "title1<script>alert('123');</script>",
        "http://foo.com<script>alert('123');</script>",
        Instant.now());
    assertEquals("title1&lt;script&gt;alert(&#39;123&#39;);&lt;/script&gt;", article.getTitle());
    assertEquals("http://foo.com&lt;script&gt;alert(&#39;123&#39;);&lt;/script&gt;",
        article.getLink());
  }

  @Test
  public void createSpeak_escape() throws Exception {
    Article article = Article.createSpeak(zone,
        zoneAlias,
        articleId,
        account,
        "title1<script>alert('123');</script>",
        "<script>alert('123');</script>",
        Instant.now());
    assertEquals("title1&lt;script&gt;alert(&#39;123&#39;);&lt;/script&gt;", article.getTitle());
    assertEquals("<script>alert('123');</script>", article.getContent());
    assertEquals("<p>&lt;script&gt;alert(&#39;123&#39;);&lt;/script&gt;</p>\n",
        article.getRenderContent());
  }

  @Test
  public void renderSpeakPreview() throws Exception {
    String content = "pixel `art` is better";
    assertEquals("<p>pixel <code>art</code> is better</p>\n", Article.renderSpeakPreview(content));
  }

  @Test
  public void getRenderContent() throws Exception {
    String content = "pixel `art` is better";
    Article speakArticle = Article.createSpeak(zone,
        zoneAlias,
        articleId,
        account,
        "title 123",
        content,
        Instant.now());
    assertTrue(speakArticle.hasMarkDownContent());
    assertEquals("<p>pixel <code>art</code> is better</p>\n", speakArticle.getRenderContent());

    Article linkArticle = Article.createExternalLink(zone,
        zoneAlias,
        articleId,
        account,
        "title 123",
        "http://foo.com",
        Instant.now());
    assertFalse(linkArticle.hasMarkDownContent());
    assertEquals("<p>pixel <code>art</code> is better</p>\n", speakArticle.getRenderContent());
    assertEquals("", linkArticle.getRenderContent());
  }

  @Test
  public void linkHintForExternal() throws Exception {
    Article externalLink = Article.createExternalLink(zone,
        zoneAlias,
        articleId,
        account,
        "title",
        "http://foo.com",
        now);

    assertEquals("foo.com", externalLink.getLinkHint());

    externalLink = Article.createExternalLink(zone,
        zoneAlias,
        articleId,
        account,
        "title",
        "httPS://bar.com/xyz.123/?999",
        now);

    assertEquals("bar.com", externalLink.getLinkHint());
  }

  @Test
  public void getShortUrlPath() throws Exception {
    assertEquals("/d/xyz1234",
        article(zone, FlakeId.fromString("xyz1234"), "foo title").getShortUrlPath());
    assertEquals("/d/cuteCate",
        article(Zone.valueOf("funny"),
            FlakeId.fromString("cuteCate"),
            "foo title").getShortUrlPath());
  }

  @Test
  public void linkHintForSpeak() throws Exception {

    Article speak = Article.createSpeak(zone,
        zoneAlias,
        articleId,
        account,
        "title",
        "oh you are my both",
        now);

    assertEquals("/z/abc", speak.getLinkHint());
  }
}