package io.kaif.model.debate;

import static java.util.stream.Collectors.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import javax.annotation.concurrent.Immutable;

import io.kaif.rank.SortingNode;
import io.kaif.rank.WilsonScore;
import io.kaif.web.v1.dto.V1DebateDto;
import io.kaif.web.v1.dto.V1DebateNodeDto;

@Immutable
public final class DebateTree {

  public static final ToDoubleFunction<Debate> DEBATE_TO_WILSON_SCORE = (debate) -> {
    if (debate == null) {
      return WilsonScore.lowerBound(0, 0);
    } else {
      return WilsonScore.lowerBound(debate.getUpVote(), debate.getDownVote());
    }
  };

  /**
   * construct a DebateTree from a properly sorted debate list, the sorting order must be depth
   * first.
   * <p>
   * if input debate list is not depth first, exception thrown.
   */
  public static DebateTree fromDepthFirst(List<Debate> flatten) {
    if (flatten.isEmpty()) {
      return new DebateTree(SortingNode.emptyRoot());
    }
    SortingNode.Builder<Debate> builder = new SortingNode.Builder<>();

    int currentLevel = flatten.get(0).getLevel() - 1;
    for (Debate debate : flatten) {
      if (debate.getLevel() == currentLevel) {
        builder = builder.siblingNode(debate);
      } else if (debate.getLevel() == currentLevel + 1) {
        builder = builder.childNode(debate);
        currentLevel++;
      } else if (debate.getLevel() < currentLevel) {
        while (debate.getLevel() < currentLevel) {
          builder = builder.parent();
          currentLevel--;
        }
        builder = builder.siblingNode(debate);
      } else {
        throw new IllegalStateException("not well flatten depth first debate list");
      }
    }
    return new DebateTree(builder.build());
  }

  /**
   * pick max score within all sub nodes, include self.
   * <p>
   * choosing the node tree based on highest score of that tree, means we sort most interesting
   * node to top. so user won't missing any good debate.
   */
  private static double maxScoreOfAllNodes(SortingNode<Debate> node,
      ToDoubleFunction<Debate> scoreCalc) {
    return node.depthFirst()
        .mapToDouble(scoreCalc)
        .max()
        .orElseGet(() -> scoreCalc.applyAsDouble(null));
  }

  private static V1DebateNodeDto deepDto(SortingNode<Debate> node) {
    V1DebateDto currentDto = Optional.ofNullable(node.getValue()).map(Debate::toV1Dto).orElse(null);
    if (!node.hasChild()) {
      return new V1DebateNodeDto(currentDto, Collections.emptyList());
    }
    List<V1DebateNodeDto> childDto = node.getChildren()
        .stream()
        .map(DebateTree::deepDto)
        .collect(toList());
    return new V1DebateNodeDto(currentDto, childDto);
  }

  private final SortingNode<Debate> node;

  public DebateTree(SortingNode<Debate> node) {
    this.node = node;
  }

  public List<SortingNode<Debate>> getChildren() {
    return node.getChildren();
  }

  public boolean isEmpty() {
    return node.hasChild();
  }

  /**
   * scoreCalc should calculate Debate's score, higher means better. the function also need to
   * handle null Debate case (means lowest score).
   * <p>
   * for example, a naive net score calculator:
   * <p>
   * <pre>
   * ToDoubleFunction<Debate> netScoreCalc = (debate) -> {
   *   if (debate == null) {
   *     return 0d;
   *   } else {
   *     return debate.getUpVote() - debate.getDownVote();
   *   }
   * }
   * </pre>
   *
   * @return new sorted DebateTree based on best score
   */
  public DebateTree sortByBestScore(ToDoubleFunction<Debate> scoreCalc) {
    SortingNode<Debate> sorted = node.deepSort((child1, child2) -> {
      double score1 = maxScoreOfAllNodes(child1, scoreCalc);
      double score2 = maxScoreOfAllNodes(child2, scoreCalc);
      return Double.compare(score2, score1);
    });
    return new DebateTree(sorted);
  }

  /**
   * sort tree by wilson score
   * <p>
   * see {@link #sortByBestScore(java.util.function.ToDoubleFunction)}
   */
  public DebateTree sortByBestScore() {
    return sortByBestScore(DEBATE_TO_WILSON_SCORE);
  }

  public Stream<Debate> depthFirst() {
    return node.depthFirst();
  }

  public V1DebateNodeDto toV1Dto() {
    return deepDto(node);
  }
}
