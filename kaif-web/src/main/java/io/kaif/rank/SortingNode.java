package io.kaif.rank;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import io.kaif.util.MoreCollectors;

@Immutable
public class SortingNode<T> {

  public static class Builder<T> {

    private final T value;
    private final List<Builder<T>> children;
    private final Builder<T> parent;

    public Builder() {
      this(null, null, new LinkedList<>());
    }

    private Builder(T value, Builder<T> parent, List<Builder<T>> children) {
      this.value = value;
      this.parent = parent;
      this.children = children;
    }

    /**
     * add child node, return child node
     */
    public Builder<T> node(T childValue) {
      Preconditions.checkNotNull(childValue, "value must not null");
      Builder<T> child = new Builder<>(childValue, this, new LinkedList<>());
      children.add(child);
      return child;
    }

    /**
     * add child node, return current node
     */
    public Builder<T> add(T childValue) {
      node(childValue);
      return this;
    }

    @Override
    public String toString() {
      return value == null ? "*" : value.toString();
    }

    public SortingNode<T> build() {
      Builder<T> root = this;
      while (root.parent() != null) {
        root = root.parent();
      }
      return root.deepBuild();
    }

    private SortingNode<T> deepBuild() {
      if (children.isEmpty()) {
        return new SortingNode<>(value, ImmutableList.of());
      }
      ImmutableList<SortingNode<T>> collect = children.stream()
          .map(new Function<Builder<T>, SortingNode<T>>() {
            @Override
            public SortingNode<T> apply(Builder<T> tBuilder) {
              return tBuilder.deepBuild();
            }
          })
          .collect(MoreCollectors.toImmutableList());
      return new SortingNode<>(value, collect);
    }

    public Builder<T> parent() {
      return parent;
    }
  }

  @Nullable
  private final T value;

  private final List<SortingNode<T>> children;

  private SortingNode(T value, ImmutableList<SortingNode<T>> children) {
    this.value = value;
    this.children = children;
  }

  @Nullable
  public T getValue() {
    return value;
  }

  public List<SortingNode<T>> getChildren() {
    return children;
  }

  public boolean hasChild() {
    return !children.isEmpty();
  }

  public SortingNode<T> deepSort(Comparator<SortingNode<T>> comparator) {
    if (!hasChild()) {
      return this;
    }
    ImmutableList<SortingNode<T>> sorted = children.stream()
        .map(child -> child.deepSort(comparator))
        .sorted(comparator)
        .collect(MoreCollectors.toImmutableList());
    return new SortingNode<>(value, sorted);
  }

  public String prettyPrint() {
    StringBuilder stringBuilder = new StringBuilder();
    prettyPrintInto(0, stringBuilder);
    return stringBuilder.toString();
  }

  private void prettyPrintInto(int level, StringBuilder stringBuilder) {
    IntStream.range(0, level).forEach(i -> stringBuilder.append("  "));
    stringBuilder.append(value == null ? "*" : value);
    children.forEach(c -> {
      stringBuilder.append("\n");
      c.prettyPrintInto(level + 1, stringBuilder);
    });
  }

  public Stream<T> depthFirst() {
    Stream<T> childStream = children.stream().flatMap(SortingNode::depthFirst);
    if (value == null) {
      return childStream;
    } else {
      return Stream.concat(Stream.of(value), childStream);
    }
  }

  public Stream<T> breathFirst() {
    return breathFirst(value != null);
  }

  private Stream<T> breathFirst(boolean includeSelf) {
    Stream<T> childStream = children.stream().map(SortingNode::getValue);
    Stream<T> all = Stream.concat(childStream,
        children.stream().flatMap(c -> c.breathFirst(false)));
    if (includeSelf) {
      return Stream.concat(Stream.of(value), all);
    } else {
      return all;
    }
  }
}
