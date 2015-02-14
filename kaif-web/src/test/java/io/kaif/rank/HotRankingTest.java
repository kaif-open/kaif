package io.kaif.rank;

import static org.junit.Assert.*;

import org.junit.Test;

import io.kaif.test.TimeFixture;

public class HotRankingTest implements TimeFixture {

  private final double precision = 0.001d;

  @Test
  public void score_by_total() throws Exception {
    assertEquals(0.000d, HotRanking.score(0, 0, dayAt(1, 1)), precision);
    assertEquals(1.000d, HotRanking.score(10, 0, dayAt(1, 1)), precision);
    assertEquals(1.176d, HotRanking.score(15, 0, dayAt(1, 1)), precision);
    assertEquals(2.000d, HotRanking.score(100, 0, dayAt(1, 1)), precision);
    assertEquals(3.000d, HotRanking.score(1000, 0, dayAt(1, 1)), precision);
    assertEquals(3.000d, HotRanking.score(2000, 1000, dayAt(1, 1)), precision);
    assertEquals(-3.000d, HotRanking.score(1000, 2000, dayAt(1, 1)), precision);
  }

  @Test
  public void score_by_time() throws Exception {
    assertEquals(0.000d, HotRanking.score(0, 0, dayAt(1, 1)), precision);
    assertEquals(1.920d, HotRanking.score(0, 0, dayAt(1, 2)), precision);
    assertEquals(3.840d, HotRanking.score(0, 0, dayAt(1, 3)), precision);
    assertEquals(59.520d, HotRanking.score(0, 0, dayAt(2, 1)), precision);
  }

  @Test
  public void score_by_mixed() throws Exception {
    assertEquals(2.920d, HotRanking.score(10, 0, dayAt(1, 2)), precision);
    assertEquals(4.539d, HotRanking.score(30, 25, dayAt(1, 3)), precision);

    assertEquals(4.840d, HotRanking.score(10, 0, dayAt(1, 3)), precision);
    assertEquals(5.840d, HotRanking.score(100, 0, dayAt(1, 3)), precision);
    assertEquals(6.840d, HotRanking.score(1000, 0, dayAt(1, 3)), precision);
    assertEquals(5.760d, HotRanking.score(0, 0, dayAt(1, 4)), precision);
    assertEquals(6.760d, HotRanking.score(10, 0, dayAt(1, 4)), precision);
  }
}