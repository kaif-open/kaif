package io.kaif.rank;

import static org.junit.Assert.*;

import org.junit.Test;

public class WilsonScoreTest {

  private double precision = 0.001d;

  @Test
  public void lowerBound() throws Exception {
    assertEquals(0, WilsonScore.lowerBound(0, 0), precision);
    assertEquals(0.722, WilsonScore.lowerBound(10, 0), precision);
    assertEquals(0.963, WilsonScore.lowerBound(100, 0), precision);
    assertEquals(0.431, WilsonScore.lowerBound(100, 100), precision);
    assertEquals(0.000, WilsonScore.lowerBound(0, 100), precision);
    assertEquals(0.167, WilsonScore.lowerBound(30, 100), precision);
    assertEquals(1.000, WilsonScore.lowerBound(Integer.MAX_VALUE, 100), precision);
  }
}