package io.kaif.flake;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Preconditions;

import io.kaif.token.Base62;

/**
 * FlakeId is similar to twitter's SnowFlake, which use 64bit long for unique id.
 * the structure of FlakeId is:
 * <p>
 * <code>
 * |--- 41 bits milli seconds --|-- 12 bits micro seconds --|-- 10 bits node id --|
 * </code>
 * <p>
 * long type is 64 bits but sign bit is not used, so meaningful bits are 63.
 * <p>
 * the timestamp part start from 2015-01-01T00:00:00Z. thus FlakeId could not use time before 2015.
 * <p>
 * for each node, FlakeId can generate 4096000 number of unique id per second
 * <p>
 * jackson serialize format is base62 string
 */
@JsonSerialize(using = FlakeIdSerializer.class)
@JsonDeserialize(using = FlakeIdDeserilaizer.class)
public final class FlakeId implements Comparable<FlakeId> {

  // 2015-01-01T00:00:00Z
  private static final long START_OF_SEQUENCE_TIME = 1420070400000L;
  private static final int SUB_MILLI_BITS = 12;
  private static final int NODE_ID_BITS = 10;
  private static final int MIN_NODE_ID = 0;

  /**
   * minimum value of FlakeId, start from 2015-01-01T00:00:00Z
   */
  public static final FlakeId MIN = new FlakeId(0, MIN_NODE_ID);

  /**
   * maximum value of FlakeId, the epoch time is 2084-09-06T15:47:35.551Z
   */
  public static final FlakeId MAX = new FlakeId(Long.MAX_VALUE);

  /**
   * remove fraction of milli seconds in sequence time
   */
  static long millisOfSequenceTime(long sequenceTime) {
    return sequenceTime >> SUB_MILLI_BITS;
  }

  static long sequenceTimeFromEpochMilli(long epochMilli) {
    return (epochMilli - START_OF_SEQUENCE_TIME) << SUB_MILLI_BITS;
  }

  public static FlakeId fromString(String base62) {
    return new FlakeId(Base62.toBase10(base62));
  }

  public static FlakeId valueOf(long value) {
    return new FlakeId(value);
  }

  /**
   * create a pseudo FlakeId with min value on specified epochMilli time. created FlakeId can use
   * to query time range in database
   */
  public static FlakeId startOf(long epochMilli) {
    long sequenceTime = sequenceTimeFromEpochMilli(epochMilli);
    return new FlakeId(sequenceTime, MIN_NODE_ID);
  }

  /**
   * create a pseudo FlakeId which max value on specified epochMilli time. created FlakeId can use
   * to query time range in database
   */
  public static FlakeId endOf(long epochMilli) {
    long sequenceTime = sequenceTimeFromEpochMilli(epochMilli);
    long maxSubMilli = (1L << SUB_MILLI_BITS) - 1;
    int maxNodeId = (1 << NODE_ID_BITS) - 1;
    return new FlakeId(sequenceTime + maxSubMilli, maxNodeId);
  }

  private final long value;

  FlakeId(long sequenceTime, int nodeId) {
    this((sequenceTime << NODE_ID_BITS) | nodeId);
  }

  private FlakeId(long value) {
    Preconditions.checkArgument(value >= 0, "invalid flakeId");
    this.value = value;
  }

  long sequenceTime() {
    return value >> NODE_ID_BITS;
  }

  int nodeId() {
    return (int) (value % (1L << NODE_ID_BITS));
  }

  /**
   * extract epoch milli part in this id
   */
  public long epochMilli() {
    return (value >> (SUB_MILLI_BITS + NODE_ID_BITS)) + START_OF_SEQUENCE_TIME;
  }

  /**
   * long value of FlakeId
   */
  public long value() {
    return value;
  }

  /**
   * Base62 string
   */
  @Override
  public String toString() {
    return Base62.fromBase10(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FlakeId flakeId = (FlakeId) o;
    return value == flakeId.value;
  }

  @Override
  public int hashCode() {
    return (int) (value ^ (value >>> 32));
  }

  @Override
  public int compareTo(FlakeId o) {
    return Long.compare(value, o.value);
  }
}

class FlakeIdSerializer extends JsonSerializer<FlakeId> {

  @Override
  public void serialize(FlakeId id, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {
    jgen.writeString(id.toString());
  }

}

class FlakeIdDeserilaizer extends JsonDeserializer<FlakeId> {

  @Override
  public FlakeId deserialize(JsonParser jp, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    String value = jp.readValueAs(String.class);
    return FlakeId.fromString(value);
  }
}