package io.kaif.util;

import static org.junit.Assert.*;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class MoreCollectorsTest {

  @Test
  public void toImmutableList() throws Exception {
    assertEquals(ImmutableList.of(), Stream.of().collect(MoreCollectors.toImmutableList()));
    assertEquals(ImmutableList.of(1, 2, 3, 4),
        Stream.of(1, 2, 3, 4).collect(MoreCollectors.toImmutableList()));
  }

  @Test
  public void toImmutableSet() throws Exception {
    assertEquals(ImmutableSet.of(), Stream.of().collect(MoreCollectors.toImmutableSet()));
    assertEquals(ImmutableSet.of(1, 2, 4, 3, -1),
        Stream.of(-1, 1, 2, 3, 4).collect(MoreCollectors.toImmutableSet()));
  }

  @Test
  public void toImmutableMap() throws Exception {
    assertEquals(ImmutableMap.of(),
        Stream.of()
            .collect(MoreCollectors.toImmutableMap(Function.identity(), Function.identity())));

    ImmutableMap<String, Integer> result = Stream.of("a,1", "b,2", "c,3")
        .collect(MoreCollectors.toImmutableMap(//
            item -> item.split(",")[0],  //
            item -> Integer.valueOf(item.split(",")[1])));

    assertEquals(ImmutableMap.of("a", 1, "b", 2, "c", 3), result);
  }
}