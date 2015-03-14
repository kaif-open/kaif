package io.kaif.model.article;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

import io.kaif.flake.FlakeId;
import io.kaif.model.zone.Zone;
import io.kaif.test.ModelFixture;

public class ArticleListTest implements ModelFixture {

  @Test
  public void timeRangeArticleId() throws Exception {
    ArticleList list = new ArticleList(Collections.<Article>emptyList());
    assertEquals(FlakeId.MIN, list.getOldestArticleId());
    assertEquals(FlakeId.MIN, list.getNewestArticleId());
    assertFalse(list.hasNext());
    assertNull(list.getLastArticleId());

    Zone zone = Zone.valueOf("abc");
    Article a1 = article(zone, "title 1");
    list = new ArticleList(asList(a1));

    assertEquals(a1.getArticleId(), list.getOldestArticleId());
    assertEquals(a1.getArticleId(), list.getNewestArticleId());
    assertEquals(a1.getArticleId(), list.getLastArticleId());
    assertTrue(list.hasNext());

    Article a2 = article(zone, "title 2");
    Article a3 = article(zone, "title 3");
    Article a4 = article(zone, "title 4");
    list = new ArticleList(asList(a4, a3, a1, a2));

    assertEquals(a1.getArticleId(), list.getOldestArticleId());
    assertEquals(a4.getArticleId(), list.getNewestArticleId());
    assertEquals(a2.getArticleId(), list.getLastArticleId());
    assertTrue(list.hasNext());
  }
}