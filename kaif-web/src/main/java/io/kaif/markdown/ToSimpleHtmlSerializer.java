package io.kaif.markdown;

import org.pegdown.LinkRenderer;
import org.pegdown.Printer;
import org.pegdown.VerbatimSerializer;
import org.pegdown.ast.AbbreviationNode;
import org.pegdown.ast.AutoLinkNode;
import org.pegdown.ast.BlockQuoteNode;
import org.pegdown.ast.BulletListNode;
import org.pegdown.ast.CodeNode;
import org.pegdown.ast.DefinitionListNode;
import org.pegdown.ast.DefinitionNode;
import org.pegdown.ast.DefinitionTermNode;
import org.pegdown.ast.ExpImageNode;
import org.pegdown.ast.ExpLinkNode;
import org.pegdown.ast.HeaderNode;
import org.pegdown.ast.HtmlBlockNode;
import org.pegdown.ast.InlineHtmlNode;
import org.pegdown.ast.ListItemNode;
import org.pegdown.ast.MailLinkNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.OrderedListNode;
import org.pegdown.ast.ParaNode;
import org.pegdown.ast.QuotedNode;
import org.pegdown.ast.RefImageNode;
import org.pegdown.ast.RefLinkNode;
import org.pegdown.ast.ReferenceNode;
import org.pegdown.ast.RootNode;
import org.pegdown.ast.SimpleNode;
import org.pegdown.ast.SpecialTextNode;
import org.pegdown.ast.StrikeNode;
import org.pegdown.ast.StrongEmphSuperNode;
import org.pegdown.ast.SuperNode;
import org.pegdown.ast.TableBodyNode;
import org.pegdown.ast.TableCaptionNode;
import org.pegdown.ast.TableCellNode;
import org.pegdown.ast.TableColumnNode;
import org.pegdown.ast.TableHeaderNode;
import org.pegdown.ast.TableNode;
import org.pegdown.ast.TableRowNode;
import org.pegdown.ast.TextNode;
import org.pegdown.ast.VerbatimNode;
import org.pegdown.ast.Visitor;
import org.pegdown.ast.WikiLinkNode;

import java.util.Map;

import static org.parboiled.common.Preconditions.checkArgNotNull;

public class ToSimpleHtmlSerializer implements Visitor {

  protected Printer printer = new Printer();
  protected final LinkRenderer linkRenderer;

  protected Map<String, VerbatimSerializer> verbatimSerializers;

  public ToSimpleHtmlSerializer(LinkRenderer linkRenderer) {
    this.linkRenderer = linkRenderer;
  }

  public String toHtml(RootNode astRoot) {
    checkArgNotNull(astRoot, "astRoot");
    astRoot.accept(this);
    return printer.getString();
  }

  public void visit(RootNode node) {
    visitChildren(node);
  }

  @Override public void visit(final AbbreviationNode node) {
    printer.print(printChildrenToString(node));
  }

  public void visit(AutoLinkNode node) {
    printLink(linkRenderer.render(node));
  }

  public void visit(BlockQuoteNode node) {
    printIndentedTag(node, "blockquote");
  }

  public void visit(BulletListNode node) {
    printIndentedTag(node, "ul");
  }

  public void visit(CodeNode node) {
    printTag(node, "code");
  }

  public void visit(DefinitionListNode node) {
    printIndentedTag(node, "dl");
  }

  public void visit(DefinitionNode node) {
    printTag(node, "dd");
  }

  public void visit(DefinitionTermNode node) {
    printTag(node, "dt");
  }

  @Override public void visit(final ExpImageNode node) {
    printer.print(printChildrenToString(node));
  }

  @Override public void visit(final ExpLinkNode node) {
    String text = printChildrenToString(node);
    printLink(linkRenderer.render(node, text));
  }

  @Override public void visit(final HeaderNode node) {
    printer.print(printChildrenToString(node));
  }

  public void visit(HtmlBlockNode node) {
    String text = node.getText();
    if (text.length() > 0) {
      printer.println();
    }
    printer.print(text);
  }

  public void visit(InlineHtmlNode node) {
    printer.print(node.getText());
  }

  public void visit(ListItemNode node) {
    printer.println();
    printTag(node, "li");
  }

  public void visit(MailLinkNode node) {
    printLink(linkRenderer.render(node));
  }

  public void visit(OrderedListNode node) {
    printIndentedTag(node, "ol");
  }

  public void visit(ParaNode node) {
    printTag(node, "p");
  }

  public void visit(QuotedNode node) {
    switch (node.getType()) {
      case DoubleAngle:
        printer.print("&laquo;");
        visitChildren(node);
        printer.print("&raquo;");
        break;
      case Double:
        printer.print("&ldquo;");
        visitChildren(node);
        printer.print("&rdquo;");
        break;
      case Single:
        printer.print("&lsquo;");
        visitChildren(node);
        printer.print("&rsquo;");
        break;
    }
  }

  public void visit(ReferenceNode node) {
    // reference nodes are not printed
  }

  @Override public void visit(final RefImageNode node) {
    printer.print(printChildrenToString(node));
  }

  @Override public void visit(final RefLinkNode node) {
    printer.print(printChildrenToString(node));
  }

  public void visit(SimpleNode node) {
    switch (node.getType()) {
      case Apostrophe:
        printer.print("&rsquo;");
        break;
      case Ellipsis:
        printer.print("&hellip;");
        break;
      case Emdash:
        printer.print("&mdash;");
        break;
      case Endash:
        printer.print("&ndash;");
        break;
      case HRule:
        printer.println().print("<hr/>");
        break;
      case Linebreak:
        printer.print("<br/>");
        break;
      case Nbsp:
        printer.print("&nbsp;");
        break;
      default:
        throw new IllegalStateException();
    }
  }

  public void visit(StrongEmphSuperNode node) {
    if (node.isClosed()) {
      if (node.isStrong()) {
        printTag(node, "strong");
      } else {
        printTag(node, "em");
      }
    } else {
      //sequence was not closed, treat open chars as ordinary chars
      printer.print(node.getChars());
      visitChildren(node);
    }
  }

  @Override public void visit(final TableBodyNode node) {
    printer.print(printChildrenToString(node));

  }

  @Override public void visit(final TableCaptionNode node) {
    printer.print(printChildrenToString(node));

  }

  @Override public void visit(final TableCellNode node) {
    printer.print(printChildrenToString(node));

  }

  @Override public void visit(final TableColumnNode node) {
    printer.print(printChildrenToString(node));

  }

  @Override public void visit(final TableHeaderNode node) {
    printer.print(printChildrenToString(node));
  }

  @Override public void visit(final TableNode node) {
    printer.print(printChildrenToString(node));
  }

  @Override public void visit(final TableRowNode node) {
    printer.print(printChildrenToString(node));
  }

  public void visit(StrikeNode node) {
    printTag(node, "del");
  }

  public void visit(VerbatimNode node) {
    VerbatimSerializer serializer = lookupSerializer(node.getType());
    serializer.serialize(node, printer);
  }

  private VerbatimSerializer lookupSerializer(final String type) {
    if (type != null && verbatimSerializers.containsKey(type)) {
      return verbatimSerializers.get(type);
    } else {
      return verbatimSerializers.get(VerbatimSerializer.DEFAULT);
    }
  }

  public void visit(WikiLinkNode node) {
    printLink(linkRenderer.render(node));
  }

  public void visit(TextNode node) {
    printer.print(node.getText());
  }

  public void visit(SpecialTextNode node) {
    printer.printEncoded(node.getText());
  }

  public void visit(SuperNode node) {
    visitChildren(node);
  }

  public void visit(Node node) {
    // override this method for processing custom Node implementations
    throw new RuntimeException("Don't know how to handle node " + node);
  }

  // helpers

  protected void visitChildren(SuperNode node) {
    for (Node child : node.getChildren()) {
      child.accept(this);
    }
  }

  protected void printTag(TextNode node, String tag) {
    printer.print('<').print(tag).print('>');
    printer.printEncoded(node.getText());
    printer.print('<').print('/').print(tag).print('>');
  }

  protected void printTag(SuperNode node, String tag) {
    printer.print('<').print(tag).print('>');
    visitChildren(node);
    printer.print('<').print('/').print(tag).print('>');
  }

  protected void printIndentedTag(SuperNode node, String tag) {
    printer.println().print('<').print(tag).print('>').indent(+2);
    visitChildren(node);
    printer.indent(-2).println().print('<').print('/').print(tag).print('>');
  }

  protected void printLink(LinkRenderer.Rendering rendering) {
    printer.print('<').print('a');
    printAttribute("href", rendering.href);
    for (LinkRenderer.Attribute attr : rendering.attributes) {
      printAttribute(attr.name, attr.value);
    }
    printer.print('>').print(rendering.text).print("</a>");
  }

  private void printAttribute(String name, String value) {
    printer.print(' ').print(name).print('=').print('"').print(value).print('"');
  }

  protected String printChildrenToString(SuperNode node) {
    Printer priorPrinter = printer;
    printer = new Printer();
    visitChildren(node);
    String result = printer.getString();
    printer = priorPrinter;
    return result;
  }

  protected String normalize(String string) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      switch (c) {
        case ' ':
        case '\n':
        case '\t':
          continue;
      }
      sb.append(Character.toLowerCase(c));
    }
    return sb.toString();
  }

}
