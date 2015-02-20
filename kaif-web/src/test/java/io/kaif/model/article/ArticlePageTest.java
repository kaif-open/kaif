package io.kaif.model.article;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

import io.kaif.flake.FlakeId;
import io.kaif.model.zone.Zone;
import io.kaif.test.ModelFixture;

public class ArticlePageTest implements ModelFixture {

  @Test
  public void timeRangeArticleId() throws Exception {
    ArticlePage page = new ArticlePage(Collections.<Article>emptyList());
    assertEquals(FlakeId.MIN, page.getOldestArticleId());
    assertEquals(FlakeId.MIN, page.getNewestArticleId());
    assertFalse(page.hasNext());
    assertNull(page.getLastArticleId());

    Zone zone = Zone.valueOf("abc");
    Article a1 = article(zone, "title 1");
    page = new ArticlePage(asList(a1));

    assertEquals(a1.getArticleId(), page.getOldestArticleId());
    assertEquals(a1.getArticleId(), page.getNewestArticleId());
    assertEquals(a1.getArticleId(), page.getLastArticleId());
    assertTrue(page.hasNext());

    Article a2 = article(zone, "title 2");
    Article a3 = article(zone, "title 3");
    Article a4 = article(zone, "title 4");
    page = new ArticlePage(asList(a4, a3, a1, a2));

    assertEquals(a1.getArticleId(), page.getOldestArticleId());
    assertEquals(a4.getArticleId(), page.getNewestArticleId());
    assertEquals(a2.getArticleId(), page.getLastArticleId());
    assertTrue(page.hasNext());
  }
}