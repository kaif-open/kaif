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
  public void createExternalLink_escape() throws Exception {
    Article article = Article.createExternalLink(zone,
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
        articleId,
        account,
        "title 123",
        content,
        Instant.now());
    assertEquals("<p>pixel <code>art</code> is better</p>\n", speakArticle.getRenderContent());

    Article linkArticle = Article.createExternalLink(zone,
        articleId,
        account,
        "title 123",
        "http://foo.com",
        Instant.now());
    assertEquals("", linkArticle.getRenderContent());
  }

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
  public void linkHintForSpeak() throws Exception {

    Article speak = Article.createSpeak(zone,
        articleId,
        account,
        "title",
        "oh you are my both",
        now);

    assertEquals("/z/abc", speak.getLinkHint());
  }
}