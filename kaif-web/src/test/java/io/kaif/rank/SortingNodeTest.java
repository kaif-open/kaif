package io.kaif.rank;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class SortingNodeTest {

  @Test
  public void buildNodeTree() throws Exception {

    SortingNode<String> root = new SortingNode.Builder<String>().build();
    assertFalse(root.hasChild());

    SortingNode<Integer> tree = new SortingNode.Builder<Integer>()//
        .add(100)
        .node(200)
        .add(210)
        .add(220)
        .parent()
        .add(300)
        .node(400)
        .node(410)
        .parent()
        .add(420)
        .node(430)
        .node(431)
        .build();

    String expected = ""
        + "*\n"
        + "  100\n"
        + "  200\n"
        + "    210\n"
        + "    220\n"
        + "  300\n"
        + "  400\n"
        + "    410\n"
        + "    420\n"
        + "    430\n"
        + "      431";
    assertEquals(expected, tree.prettyPrint());
  }

  @Test
  public void depthFirst() throws Exception {
    SortingNode<Integer> tree = new SortingNode.Builder<Integer>()//
        .add(100)
        .node(200)
        .add(210)
        .add(220)
        .parent()
        .add(300)
        .node(400)
        .node(410)
        .parent()
        .add(420)
        .node(430)
        .node(431)
        .build();

    assertEquals(asList(100, 200, 210, 220, 300, 400, 410, 420, 430, 431),
        tree.depthFirst().collect(toList()));

    SortingNode<Integer> n400 = tree.getChildren().get(3);
    assertEquals(asList(400, 410, 420, 430, 431), n400.depthFirst().collect(toList()));
  }

  @Test
  public void breathFirst() throws Exception {
    SortingNode<Integer> tree = new SortingNode.Builder<Integer>()//
        .add(100)
        .node(200)
        .add(210)
        .add(220)
        .parent()
        .add(300)
        .node(400)
        .node(410)
        .parent()
        .add(420)
        .node(430)
        .node(431)
        .build();

    assertEquals(asList(100, 200, 300, 400, 210, 220, 410, 420, 430, 431),
        tree.breathFirst().collect(toList()));

    SortingNode<Integer> n200 = tree.getChildren().get(1);
    assertEquals(asList(200, 210, 220), n200.breathFirst().collect(toList()));
  }

  @Test
  public void deepSort() throws Exception {

    SortingNode<Integer> tree = new SortingNode.Builder<Integer>()//
        .add(200)
        .node(100)
        .add(110)
        .add(120)
        .parent()
        .add(300)
        .node(400)
        .node(420)
        .parent()
        .add(410)
        .node(430)
        .node(431)
        .build();

    tree = tree.deepSort((a, b) -> b.getValue() - a.getValue());
    String expected = ""
        + "*\n"
        + "  400\n"
        + "    430\n"
        + "      431\n"
        + "    420\n"
        + "    410\n"
        + "  300\n"
        + "  200\n"
        + "  100\n"
        + "    120\n"
        + "    110";
    assertEquals(expected, tree.prettyPrint());
  }
}