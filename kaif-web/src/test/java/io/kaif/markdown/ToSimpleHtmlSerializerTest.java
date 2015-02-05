package io.kaif.markdown;

import org.junit.Before;
import org.junit.Test;
import org.pegdown.Extensions;
import org.pegdown.LinkRenderer;
import org.pegdown.PegDownProcessor;

import static org.junit.Assert.assertEquals;

public class ToSimpleHtmlSerializerTest {

  private PegDownProcessor pegDownProcessor;
  private ToSimpleHtmlSerializer htmlSerializer;

  @Before
  public void setup() {
    pegDownProcessor = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS);
    htmlSerializer = new ToSimpleHtmlSerializer(new LinkRenderer());
  }

  @Test
  public void convertToHtml_simple() {
    assertEquals(""
            + "<a href=\"http://example.com/\">http://example.com/</a>"
            + "hihi<p>Dillinger is a cloud-enabled, mobile-ready, offline-storage, AngularJS powered HTML5 Markdown editor.</p>\n"
            + "<ul>\n"
            + "  <li>Type some Markdown on the left</li>\n"
            + "  <li>See HTML in the right</li>\n"
            + "  <li>Magic</li>\n"
            + "</ul><p>This text you see here is <em>actually</em> written in Markdown! To get a feel for Markdown's syntax, type some text into the left window and watch the results in the right.</p>",
        htmlSerializer.toHtml(pegDownProcessor.parseMarkdown((""
            + "<http://example.com/>"
            + "hihi\n"
            + "===\n"
            + "\n"
            + "\n"
            + "\n"
            + "Dillinger is a cloud-enabled, mobile-ready, offline-storage, AngularJS powered HTML5 Markdown editor.\n"
            + "\n"
            + "  - Type some Markdown on the left\n"
            + "  - See HTML in the right\n"
            + "  - Magic\n"
            + "\n"
            + "This text you see here is *actually* written in Markdown! To get a feel for Markdown's syntax, type some text into the left window and watch the results in the right."
        ).toCharArray())));
  }

}