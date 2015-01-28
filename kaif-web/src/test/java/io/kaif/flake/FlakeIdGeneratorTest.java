package io.kaif.flake;

import static org.junit.Assert.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

import com.google.common.collect.Sets;

public class FlakeIdGeneratorTest {

  static class DummyFlakeGenerator extends FlakeIdGenerator {
    protected DummyFlakeGenerator(int nodeId, Clock clock, String scope) {
      super(nodeId, clock, scope);
    }

    //this is like production
    private DummyFlakeGenerator() {
      super(445);
    }
  }

  private Clock fixed(int month, int day) {
    Instant now = LocalDate.of(2015, month, day).atStartOfDay(ZoneOffset.UTC).toInstant();
    return Clock.fixed(now, ZoneOffset.UTC);
  }

  @Test
  public void generate() throws Exception {
    DummyFlakeGenerator gen12 = new DummyFlakeGenerator(12, fixed(1, 2), "generate");
    assertEquals(12, gen12.getNodeId());
    FlakeId flakeId = gen12.next();

    //2015/01/02 UTC
    assertEquals(1420156800000L, flakeId.epochMilli());
    assertEquals(353894400000L, flakeId.sequenceTime());
    assertEquals(362387865600012L, flakeId.value());
    assertEquals(12, flakeId.nodeId());
  }

  @Test
  public void generatorWithDifferentTime() throws Exception {
    FlakeId id3 = new DummyFlakeGenerator(1, fixed(1, 3), "gen3").next();
    assertEquals(1420243200000L, id3.epochMilli());
    assertEquals(724775731200001L, id3.value());
    FlakeId id4 = new DummyFlakeGenerator(1, fixed(1, 4), "gen4").next();
    assertEquals(1420329600000L, id4.epochMilli());
    assertEquals(1087163596800001L, id4.value());
  }

  @Test
  public void generateWithSubMilliSecond() throws Exception {
    DummyFlakeGenerator generator = new DummyFlakeGenerator(1, fixed(10, 3), "genSub");
    List<FlakeId> flakeIds = IntStream.range(0, 4096)
        .mapToObj(i -> generator.next())
        .collect(Collectors.toList());
    assertEquals("flake id generated on the same time should different in sub millisecond",
        4096,
        Sets.newHashSet(flakeIds).size());

    assertEquals(99656663040000001L, flakeIds.get(0).value());
    assertEquals(99656663044193281L, flakeIds.get(4095).value());
  }

  @Test
  public void generateExceedSubMilliSecond() throws Exception {
    DummyFlakeGenerator production = new DummyFlakeGenerator();

    Set<FlakeId> flakeIds = IntStream.range(0, 1024000)
        .mapToObj(i -> production.next())
        .collect(Collectors.toSet());
    assertEquals(1024000, flakeIds.size());
    assertEquals(445, flakeIds.stream().findFirst().get().nodeId());
  }

  @Test
  public void generateWithOrder() throws Exception {
    DummyFlakeGenerator production = new DummyFlakeGenerator();

    List<FlakeId> flakeIds = IntStream.range(0, 10240)
        .mapToObj(i -> production.next())
        .collect(Collectors.toList());
    assertEquals(10240, flakeIds.size());

    ArrayList<FlakeId> oldOrders = new ArrayList<>(flakeIds);
    Collections.shuffle(flakeIds);
    Collections.sort(flakeIds);
    assertEquals(oldOrders, flakeIds);
  }

  @Test
  public void generatorWithDifferentNodeId() throws Exception {
    DummyFlakeGenerator gen1001 = new DummyFlakeGenerator(1001, fixed(1, 2), "gen2");
    assertEquals(1001, gen1001.getNodeId());

    FlakeId flakeId = gen1001.next();
    //2015/01/02 UTC
    assertEquals(1420156800000L, flakeId.epochMilli());

    assertEquals(362387865601001L, flakeId.value());
  }

  @Test
  public void base62() throws Exception {
    DummyFlakeGenerator generator = new DummyFlakeGenerator(1001, fixed(4, 3), "gen43");

    FlakeId flakeId = generator.next();
    assertEquals("cCRjZpXdiL", flakeId.toString());
    assertEquals(flakeId, FlakeId.fromString("cCRjZpXdiL"));

    FlakeId next = generator.next();
    assertEquals("cCRjZpXdzh", next.toString());
    assertEquals(next, FlakeId.fromString("cCRjZpXdzh"));
  }

  @Test
  public void startOf() throws Exception {
    assertEquals(0L, FlakeId.MIN.value());
    Instant now = LocalDate.of(2016, 10, 11).atStartOfDay(ZoneOffset.UTC).toInstant();
    FlakeId start = FlakeId.startOf(now.toEpochMilli());
    assertEquals(0, start.nodeId());
    assertEquals(235189724774400000L, start.value());
    assertEquals(229677465600000L, start.sequenceTime());

    //not allow epoch milli before 2015
    long before2015 = 1420000000000L;
    try {
      FlakeId.startOf(before2015);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void endOf() throws Exception {
    assertEquals(Long.MAX_VALUE, FlakeId.MAX.value());
    Instant now = LocalDate.of(2050, 8, 31).atStartOfDay(ZoneOffset.UTC).toInstant();
    FlakeId end = FlakeId.endOf(now.toEpochMilli());
    assertEquals(1023, end.nodeId());
    assertEquals(4720464337309794303L, end.value());
    assertEquals(4609828454404095L, end.sequenceTime());

    //not allow epoch milli before 2015
    long before2015 = 1420000000000L;
    try {
      FlakeId.endOf(before2015);
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
    }
  }
}