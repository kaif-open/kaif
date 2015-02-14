package io.kaif.rank;

import static java.lang.Math.abs;
import static java.lang.Math.log10;
import static java.lang.Math.max;

import java.time.Duration;
import java.time.Instant;

/**
 * see also http://amix.dk/blog/post/19588
 */
public class HotRanking {

  //reddit: 1134028003 - Thu, 08 Dec 2005 07:46:43 GMT
  //   our: 1420070400 - Thu, 01 Jan 2015 00:00:00 GMT
  private static final Instant BASE_TIME = Instant.ofEpochSecond(1420070400);

  private static long total(long upVoted, long downVoted) {
    return upVoted - downVoted;
  }

  /**
   * 10 up vote = 1
   * 100 up vote = 2
   * 24 hr score = 1.92
   * <p>
   * so to keep an article on tomorrow, it has to be ~100 up voted
   * <p>
   * python:
   * <p>
   * <pre>
   * s = score(ups, downs)
   * order = log(max(abs(s), 1), 10)
   * sign = 1 if s > 0 else -1 if s < 0 else 0
   * seconds = epoch_seconds(date) - 1134028003
   * return round(sign * order + seconds / 45000, 7)
   * </pre>
   */
  public static double score(long upVoted, long downVoted, Instant createTime) {
    long s = total(upVoted, downVoted);
    double order = log10(max(abs(s), 1));
    double sign = Long.signum(s);
    long seconds = Duration.between(BASE_TIME, createTime).getSeconds();
    return sign * order + seconds / 45000.0;
  }
}
