package io.kaif.model.debate;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.function.ToDoubleFunction;

import org.junit.Test;

import io.kaif.model.article.Article;
import io.kaif.model.zone.Zone;
import io.kaif.test.ModelFixture;

public class DebateTreeTest implements ModelFixture {

  private Article article = article(Zone.valueOf("fun"), "art-1");

  @Test
  public void fromDepthFirst_empty() throws Exception {
    DebateTree debateTree = DebateTree.fromDepthFirst(Collections.emptyList());
    assertEquals(0, debateTree.depthFirst().count());
  }

  @Test
  public void sortByBestScore() throws Exception {
    Debate d1 = debate(null).withVote(100, 0);
    Debate d1_1 = debate(d1).withVote(80, 0);
    Debate d1_1_1 = debate(d1_1).withVote(60, 0);
    Debate d1_1_2 = debate(d1_1).withVote(120, 0);
    Debate d1_2 = debate(d1).withVote(150, 0);
    Debate d2 = debate(null).withVote(20, 0);
    Debate d2_1 = debate(d2).withVote(250, 0);
    Debate d2_1_1 = debate(d2_1).withVote(0, 0);
    Debate d2_2 = debate(d2).withVote(40, 0);
    Debate d3 = debate(null).withVote(10, 0);
    Debate d3_1 = debate(d3).withVote(3, 0);
    List<Debate> flatten = asList(//
        d1, d1_1, d1_1_1, d1_1_2, d1_2, //
        d2, d2_1, d2_1_1, d2_2, //
        d3, d3_1);

    DebateTree debateTree = DebateTree.fromDepthFirst(flatten);

    //calc only up vote only
    ToDoubleFunction<Debate> upVoteCalc = (debate) -> debate == null ? 0 : debate.getUpVote();
    DebateTree sorted = debateTree.sortByBestScore(upVoteCalc);

    List<Debate> bestScore = asList(//
        d2, d2_1, d2_1_1, d2_2, //
        d1, d1_2, d1_1, d1_1_2, d1_1_1, //
        d3, d3_1);
    assertEquals(bestScore, sorted.depthFirst().collect(toList()));
  }

  @Test
  public void sortByBestScore_wilson() throws Exception {
    Debate d1 = debate(null).withVote(100, 30);
    Debate d2 = debate(null).withVote(20, 30);
    Debate d2_1 = debate(d2).withVote(25, 3); //highest in wilson, > d1
    Debate d2_2 = debate(d2).withVote(40, 30);
    Debate d3 = debate(null).withVote(10, 30);
    Debate d3_1 = debate(d3).withVote(3, 30);
    List<Debate> flatten = asList(//
        d1,//
        d2, d2_1, d2_2, //
        d3, d3_1);

    DebateTree debateTree = DebateTree.fromDepthFirst(flatten);

    DebateTree sorted = debateTree.sortByBestScore();

    List<Debate> bestScore = asList(//
        d2, d2_1, d2_2, //
        d1,//
        d3, d3_1);
    assertEquals(bestScore, sorted.depthFirst().collect(toList()));
  }

  @Test
  public void fromDepthFirst() throws Exception {
    Debate d1 = debate(null);
    Debate d1_1 = debate(d1);
    Debate d1_1_1 = debate(d1_1);
    Debate d1_1_2 = debate(d1_1);
    Debate d1_2 = debate(d1);
    Debate d2 = debate(null);
    Debate d2_1 = debate(d2);
    Debate d2_1_1 = debate(d2_1);
    Debate d2_2 = debate(d2);
    Debate d3 = debate(null);
    Debate d3_1 = debate(d3);

    List<Debate> flatten = asList(//
        d1, d1_1, d1_1_1, d1_1_2, d1_2, //
        d2, d2_1, d2_1_1, d2_2, //
        d3, d3_1);

    DebateTree debateTree = DebateTree.fromDepthFirst(flatten);
    assertEquals(flatten, debateTree.depthFirst().collect(toList()));
  }

  private Debate debate(Debate parent) {
    return debate(article, "", parent);
  }

}