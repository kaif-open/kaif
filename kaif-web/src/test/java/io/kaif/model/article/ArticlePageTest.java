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
  public void startEndArticleId() throws Exception {
    ArticlePage page = new ArticlePage(Collections.<Article>emptyList());
    assertEquals(FlakeId.MIN, page.getStartArticleId());
    assertEquals(FlakeId.MIN, page.getEndArticleId());

    Zone zone = Zone.valueOf("abc");
    Article a1 = article(zone, "t1");
    page = new ArticlePage(asList(a1));

    assertEquals(a1.getArticleId(), page.getStartArticleId());
    assertEquals(a1.getArticleId(), page.getEndArticleId());

    Article a2 = article(zone, "t2");
    Article a3 = article(zone, "t3");
    Article a4 = article(zone, "t4");
    page = new ArticlePage(asList(a4, a3, a1, a2));

    assertEquals(a1.getArticleId(), page.getStartArticleId());
    assertEquals(a4.getArticleId(), page.getEndArticleId());
  }
}