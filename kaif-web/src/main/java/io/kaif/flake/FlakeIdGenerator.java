package io.kaif.flake;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.annotations.VisibleForTesting;

/**
 * for every unique id domain usage, you should create it's own sub class, for example:
 * <p>
 * you have two domains that require unique id, and they are allowed to overlay (because different
 * domain), you can create two subclass:
 * <p>
 * CommentFlakeIdGenerator
 * ArticleFlakeIdGenerator
 * <p>
 * CommentFlakeIdGenerator is guarantee all flakeId generated for model (Comment) are unique. so
 * does ArticleFlakeIdGenerator. however, two types of generator may generate duplicate flakeId.
 * <p>
 * the same type of FlakeIdGenerator guarantee generated flakeIds are unique, no matter how many
 * instances of generator.
 */
public abstract class FlakeIdGenerator {

  private static final ConcurrentHashMap<String, AtomicLong> lastSequenceTimeByScope = new ConcurrentHashMap<>();

  private static AtomicLong buildLastSequenceTime(String scope) {
    return lastSequenceTimeByScope.computeIfAbsent(scope, (theScope) -> new AtomicLong());
  }

  private final int nodeId;
  private final Clock clock;
  private final AtomicLong lastSequenceTime;

  protected FlakeIdGenerator(int nodeId) {
    this.nodeId = nodeId;
    this.clock = null;
    this.lastSequenceTime = buildLastSequenceTime(getClass().toString());
  }

  @VisibleForTesting
  FlakeIdGenerator(int nodeId, Clock clock, String scope) {
    this.nodeId = nodeId;
    this.clock = clock;
    this.lastSequenceTime = buildLastSequenceTime(scope);
  }

  private long currentEpochMilli() {
    if (clock == null) {
      return System.currentTimeMillis();
    }
    return Instant.now(clock).toEpochMilli();
  }

  public final FlakeId next() {
    return new FlakeId(getCurrentSequenceTime(), nodeId);
  }

  /*
   * Note that currently we use System.currentTimeMillis() for a base time in
   * milliseconds, and then if we are in the same milliseconds that the
   * previous generation, we increment the number of sub milli second.
   * However, since the precision is 4096/milli-second (12 bit), we can only
   * generate 4096 FlakeId within a millisecond safely. If we detect we have
   * already generated that much FlakeId within a millisecond (which, while
   * admittedly unlikely in a real application, is very achievable on even
   * modest machines), then we stall the generator (busy spin) until the next
   * millisecond as required.
   */
  private long getCurrentSequenceTime() {
    while (true) {
      long now = FlakeId.sequenceTimeFromEpochMilli(currentEpochMilli());
      long last = lastSequenceTime.get();
      if (now > last) {
        if (lastSequenceTime.compareAndSet(last, now)) {
          return now;
        }
      } else {
        long lastMillis = FlakeId.millisOfSequenceTime(last);
        // If the clock went back in time, bail out
        if (FlakeId.millisOfSequenceTime(now) < FlakeId.millisOfSequenceTime(last)) {
          return lastSequenceTime.incrementAndGet();
        }

        long candidate = last + 1;
        // If we've generated more than 4096 FlakeId in that millisecond,
        // we restart the whole process until we get to the next millis.
        // Otherwise, we try use our candidate ... unless we've been
        // beaten by another thread in which case we try again.
        if (FlakeId.millisOfSequenceTime(candidate) == lastMillis && lastSequenceTime.compareAndSet(
            last,
            candidate)) {
          return candidate;
        }
      }
    }
  }

  public final int getNodeId() {
    return nodeId;
  }
}
