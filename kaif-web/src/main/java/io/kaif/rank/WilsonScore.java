package io.kaif.rank;

import org.apache.commons.math3.stat.interval.WilsonScoreInterval;

/**
 * reference: http://www.evanmiller.org/how-not-to-sort-by-average-rating.html
 */
public class WilsonScore {

  public static final double CONFIDENCE_LEVEL = 0.95d;

  /**
   * valid value is 0.0 ~ 1.0, higher is better.
   */
  public static double lowerBound(long upVoted, long downVoted) {
    if (upVoted == 0 && downVoted == 0) {
      return 0d;
    }
    long sum = upVoted + downVoted;
    if (sum >= Integer.MAX_VALUE) {
      //this is impossible! only up vote in YouTube can break 32bit limit in the world!
      return 1d;
    }
    return new WilsonScoreInterval().createInterval((int) sum, (int) upVoted, CONFIDENCE_LEVEL)
        .getLowerBound();
  }

  private WilsonScore() {
  }
}
