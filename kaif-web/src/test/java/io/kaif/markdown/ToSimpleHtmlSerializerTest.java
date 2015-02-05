package io.kaif.markdown;

import org.junit.Before;
import org.junit.Test;
import org.pegdown.Extensions;
import org.pegdown.LinkRenderer;
import org.pegdown.PegDownProcessor;

import static org.junit.Assert.assertEquals;

public class ToSimpleHtmlSerializerTest {

  private PegDownProcessor pegDownProcessor;

  @Before
  public void setup() {
    pegDownProcessor = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS);
  }

  ToSimpleHtmlSerializer createToSimpleHtmlSerializer() {
    return new ToSimpleHtmlSerializer(new LinkRenderer());
  }

  @Test
  public void visit_simple() {
    assertEquals(""
            + "<a href=\"http://example.com/\">http://example.com/</a>"
            + "hihi<p>Dillinger is a cloud-enabled, mobile-ready, offline-storage, AngularJS powered HTML5 Markdown editor.</p>\n"
            + "<ul>\n"
            + "  <li>Type some Markdown on the left</li>\n"
            + "  <li>See HTML in the right</li>\n"
            + "  <li>Magic</li>\n"
            + "</ul><p>This text you see here is <em>actually</em> written in Markdown! To get a feel for Markdown's syntax, type some text into the left window and watch the results in the right.</p>",
        createToSimpleHtmlSerializer().toHtml(pegDownProcessor.parseMarkdown((""
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
            + "\n"
            + "This text you see here is *actually* written in Markdown! \n"
            + "To get a feel for Markdown's syntax, type some text into the left window and watch the results in the right."
        ).toCharArray())));
  }

  @Test
  public void visitImage_no_effect() {
    assertEquals("<p>Creative Commons License(http://dummyimage.com/88x31.png)</p>",
        createToSimpleHtmlSerializer().toHtml(pegDownProcessor.parseMarkdown(("![Creative Commons License](http://dummyimage.com/88x31.png)")
            .toCharArray())));
  }

  @Test
  public void visit_escape() {
    assertEquals("<p>&lt;</p>",
        createToSimpleHtmlSerializer().toHtml(pegDownProcessor.parseMarkdown(("&lt;")
            .toCharArray())));

  }
  //TODO test what kaif will allow

}