package io.kaif.rank;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.UnaryOperator;
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
    private final List<Builder<T>> childBuilders;
    private final Builder<T> parentBuilder;

    public Builder() {
      this(null, null, new LinkedList<>());
    }

    private Builder(T value, Builder<T> parentBuilder, List<Builder<T>> childBuilders) {
      this.value = value;
      this.parentBuilder = parentBuilder;
      this.childBuilders = childBuilders;
    }

    /**
     * add child node, return child node builder
     */
    public Builder<T> node(T childValue) {
      Preconditions.checkNotNull(childValue, "value must not null");
      Builder<T> child = new Builder<>(childValue, this, new LinkedList<>());
      childBuilders.add(child);
      return child;
    }

    /**
     * add child node, return current node builder
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
      Builder<T> rootBuilder = this;
      while (rootBuilder.parent() != null) {
        rootBuilder = rootBuilder.parent();
      }
      return rootBuilder.deepBuild(null);
    }

    private SortingNode<T> deepBuild(SortingNode<T> parentNode) {
      if (childBuilders.isEmpty()) {
        return new SortingNode<>(value, parentNode, Stream.empty());
      }
      return new SortingNode<>(value,
          parentNode,
          childBuilders.stream().map(childBuilder -> childBuilder::deepBuild));
    }

    public Builder<T> parent() {
      return parentBuilder;
    }
  }

  @Nullable
  private final T value;

  @Nullable
  private final SortingNode<T> parent;

  private final List<SortingNode<T>> children;

  /**
   * childrenFactory accept `this` node as a argument (its parent), and produce a child node
   */
  private SortingNode(T value,
      SortingNode<T> parent,
      Stream<UnaryOperator<SortingNode<T>>> childrenFactory) {
    this.value = value;
    this.parent = parent;
    this.children = childrenFactory.map(f -> f.apply(this))
        .collect(MoreCollectors.toImmutableList());
  }

  private SortingNode(T value, SortingNode<T> parent, ImmutableList<SortingNode<T>> children) {
    this.value = value;
    this.parent = parent;
    this.children = children;
  }

  @Nullable
  public T getValue() {
    return value;
  }

  @Nullable
  public SortingNode<T> getParent() {
    return parent;
  }

  public boolean hasParent() {
    return parent != null;
  }

  public boolean isRoot() {
    return parent == null;
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
    return new SortingNode<>(value, parent, sorted);
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

  /**
   * traverse self and each children, depth first.
   * <p>
   * note that if self is root node, it is skipped (value of root node is always null)
   */
  public Stream<T> depthFirst() {
    Stream<T> childStream = children.stream().flatMap(SortingNode::depthFirst);
    if (value == null) {
      return childStream;
    } else {
      return Stream.concat(Stream.of(value), childStream);
    }
  }

  /**
   * traverse self and each children, breath first.
   * <p>
   * note that if self is root node, it is skipped (value of root node is always null)
   * <p>
   * {@link #depthFirst} is faster. prefer it unless you need breathFirst traversal.
   */
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
