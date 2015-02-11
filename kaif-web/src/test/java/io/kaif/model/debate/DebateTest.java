package io.kaif.model.debate;

import static org.junit.Assert.*;

import java.time.Instant;

import org.junit.Test;

import io.kaif.flake.FlakeId;
import io.kaif.model.article.Article;
import io.kaif.model.zone.Zone;
import io.kaif.test.ModelFixture;

public class DebateTest implements ModelFixture {

  @Test
  public void debateWithLink() throws Exception {
    Article article = article(Zone.valueOf("foo"), "t1");

    String content = "pixel art is better at [9gaga][1]\n\n[1]: http://www.google.com";
    Debate debate = Debate.create(article,
        FlakeId.fromString("aabbccdd"),
        null,
        content,
        accountCitizen("debater1"),
        Instant.now());

    assertEquals(
        "<p>pixel art is better at <a href=\"#bbccdd-1\" class=\"reference-link\">9gaga</a></p>\n"
            + "<p class=\"reference-appendix-block\"><span class=\"reference-appendix-index\">1</span>"
            + "<a id=\"bbccdd-1\"></a><a href=\"http://www.google.com\" rel=\"nofollow\">"
            + "http://www.google.com</a><br>\n"
            + "</p>",
        debate.getRenderContent());
  }
}