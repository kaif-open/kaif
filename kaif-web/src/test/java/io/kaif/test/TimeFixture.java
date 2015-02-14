package io.kaif.test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public interface TimeFixture {

  /**
   * UTC time of 2015
   */
  default Instant dayAt(int month, int day) {
    return OffsetDateTime.of(2015, month, day, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
  }

  /**
   * UTC time of 2015
   */
  default Instant minuteAt(int month, int day, int hour, int minute) {
    return OffsetDateTime.of(2015, month, day, hour, minute, 0, 0, ZoneOffset.UTC).toInstant();
  }
}
