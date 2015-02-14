package io.kaif.model.article;

import static org.junit.Assert.*;

import java.time.Instant;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.rank.HotRanking;
import io.kaif.test.DbIntegrationTests;

public class ArticleDaoTest extends DbIntegrationTests {
  @Autowired
  private ArticleDao dao;

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
}