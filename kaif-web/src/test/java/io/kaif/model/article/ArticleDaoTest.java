package io.kaif.model.article;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.rank.HotRanking;
import io.kaif.test.DbIntegrationTests;

public class ArticleDaoTest extends DbIntegrationTests {
  @Autowired
  private ArticleDao dao;
  private Account account;

  @Test
  public void hotRanking() throws Exception {
    assertHotRanking(0, 0, dayAt(1, 1));
    assertHotRanking(10, 20, dayAt(3, 1));
    assertHotRanking(390, 100, dayAt(12, 9));
    assertHotRanking(2390, 81, dayAt(5, 25));
  }

  private void assertHotRanking(long upVoted, long downVoted, Instant createTime) {
    assertEquals(HotRanking.score(upVoted, downVoted, createTime),
        dao.hotRanking(upVoted, downVoted, createTime),
        0.001d);
  }

  @Test
  public void listHotZonesCacheForSameSize() throws Exception {
    ZoneInfo z1 = savedZoneDefault("zone1");
    savedArticle(z1, account, "foo-title");
    Instant articleSince = Instant.now().minus(Duration.ofDays(20));
    List<ZoneInfo> cachedForSize10 = dao.listHotZonesWithCache(10, articleSince);
    assertSame(cachedForSize10, dao.listHotZonesWithCache(10, articleSince));
    assertNotSame(cachedForSize10, dao.listHotZonesWithCache(20, articleSince));
  }

  @Test
  public void loadArticle_cache() throws Exception {
    ZoneInfo z1 = savedZoneDefault("zone1");
    Article article = savedArticle(z1, account, "foo-title");
    Article cached = dao.loadArticleWithCache(article.getArticleId());
    assertEquals(article, cached);
    assertSame(cached, dao.loadArticleWithCache(article.getArticleId()));

    // modify trigger evict
    dao.markAsDeleted(article);
    assertNotSame(cached, dao.loadArticleWithCache(article.getArticleId()));
  }

  @Test
  public void listHotZones() throws Exception {
    assertEquals(0, dao.listHotZonesWithCache(999, Instant.now()).size());
    ZoneInfo z1 = savedZoneDefault("zone1");
    ZoneInfo z2 = savedZoneDefault("zone2");
    ZoneInfo z3 = savedZoneDefault("zone3");
    ZoneInfo k = savedZoneKaif("kaif-x");
    savedZoneDefault("zone4"); //no article

    Instant twoDaysAgo = dayAt(3, 1);
    Instant yesterday = dayAt(3, 2);
    Instant today = dayAt(3, 3);
    savedArticleWithId(z1, twoDaysAgo.minusSeconds(1));
    savedArticleWithId(z1, twoDaysAgo.minusSeconds(2));
    savedArticleWithId(z1, yesterday.plusSeconds(1));
    savedArticleWithId(z1, yesterday.plusSeconds(2));
    savedArticleWithId(z1, today.plusSeconds(1));
    savedArticleWithId(z1, today.plusSeconds(2));

    savedArticleWithId(z2, today.plusSeconds(3));
    savedArticleWithId(z3, today.plusSeconds(4));
    savedArticleWithId(z3, today.plusSeconds(5));
    savedArticleWithId(z3, today.plusSeconds(6));

    savedArticleWithId(k, yesterday.plusSeconds(3));

    assertEquals(asList(z1, z3, z2), dao.listHotZonesWithCache(10, yesterday));
    assertEquals(asList(z1, z3), dao.listHotZonesWithCache(2, yesterday));
    assertEquals(asList(z3, z1, z2), dao.listHotZonesWithCache(20, today));
  }

  @Before
  public void setUp() throws Exception {
    account = savedAccountCitizen("foo");
    dao.evictAllCaches();
  }

  private Article savedArticleWithId(ZoneInfo zoneInfo, Instant time) {
    Article article = Article.createExternalLink(zoneInfo.getZone(),
        zoneInfo.getAliasName(),
        FlakeId.startOf(time.toEpochMilli()),
        account,
        "title x",
        "http://foo.com",
        time);
    dao.insertArticle(article);
    return article;
  }
}