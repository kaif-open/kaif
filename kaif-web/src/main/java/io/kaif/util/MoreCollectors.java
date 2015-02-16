package io.kaif.util;

import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class MoreCollectors {

  public static <T> Collector<T, ?, ImmutableList<T>> toImmutableList() {
    return Collector.of(ImmutableList::<T>builder,
        ImmutableList.Builder<T>::add,
        (left, right) -> left.addAll(right.build()),
        ImmutableList.Builder::build);
  }

  public static <T> Collector<T, ?, ImmutableSet<T>> toImmutableSet() {
    return Collector.of(ImmutableSet::<T>builder,
        ImmutableSet.Builder<T>::add,
        (left, right) -> left.addAll(right.build()),
        ImmutableSet.Builder::build);
  }

  /**
   * keyMapper result must be unique
   */
  public static <T, K, U> Collector<T, ?, ImmutableMap<K, U>> toImmutableMap(//
      Function<? super T, ? extends K> keyMapper, //
      Function<? super T, ? extends U> valueMapper) {

    return Collector.of(ImmutableMap::<K, U>builder,
        (ImmutableMap.Builder<K, U> builder, T item) -> {
          builder.put(keyMapper.apply(item), valueMapper.apply(item));
        },
        throwingMerger(),
        ImmutableMap.Builder::build);
  }

  /**
   * copy from jdk Collectors, for toMap only
   */
  private static <T> BinaryOperator<T> throwingMerger() {
    return (u, v) -> {
      throw new IllegalStateException(String.format("Duplicate key %s", u));
    };
  }

  private MoreCollectors() {
  }
}
