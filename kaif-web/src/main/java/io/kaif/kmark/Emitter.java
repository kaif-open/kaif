/*
 * Copyright (C) 2011 René Jeschke <rene_jeschke@yahoo.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.kaif.kmark;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.google.common.base.Strings;

import io.kaif.model.account.Account;

/**
 * Emitter class responsible for generating HTML output.
 *
 * @author René Jeschke <rene_jeschke@yahoo.de>
 */
class Emitter {

  /**
   * Turns every whitespace character into a space character.
   *
   * @param c
   *     Character to check
   * @return 32 is c was a whitespace, c otherwise
   */
  private static char whitespaceToSpace(char c) {
    return Character.isWhitespace(c) ? ' ' : c;
  }

  /**
   * Link references.
   */
  private final LinkedHashMap<String, LinkRef> linkRefs = new LinkedHashMap<>();
  /**
   * The configuration.
   */
  private final Configuration config;

  /**
   * Constructor.
   */
  public Emitter(final Configuration config) {
    this.config = config;
  }

  /**
   * Adds a LinkRef to this set of LinkRefs.
   *
   * @param key
   *     The key/id.
   */
  public LinkRef addLinkRef(final String key, final String link, final String title) {
    final String lowerCase = key.toLowerCase();
    final LinkRef linkRef;
    if (this.linkRefs.containsKey(lowerCase)) {
      linkRef = new LinkRef(this.linkRefs.get(lowerCase).seqNumber, link, title);
    } else {
      linkRef = new LinkRef(this.linkRefs.size() + 1, link, title);
    }
    this.linkRefs.put(lowerCase, linkRef);
    return linkRef;
  }

  /**
   * Transforms the given block recursively into HTML.
   *
   * @param out
   *     The StringBuilder to write to.
   * @param root
   *     The Block to process.
   */
  public void emit(final HtmlEscapeStringBuilder out, final Block root) {
    root.removeSurroundingEmptyLines();

    switch (root.type) {
      case NONE:
        break;
      case PARAGRAPH:
        this.config.decorator.openParagraph(out);
        break;
      case BLOCKQUOTE:
        this.config.decorator.openBlockquote(out);
        break;
      case FENCED_CODE:
        if (this.config.codeBlockEmitter == null) {
          this.config.decorator.openCodeBlock(out);
        }
        break;
      case UNORDERED_LIST:
        this.config.decorator.openUnorderedList(out);
        break;
      case ORDERED_LIST:
        this.config.decorator.openOrderedList(out);
        break;
      case LIST_ITEM:
        this.config.decorator.openListItem(out);
        out.appendHtml('>');
        break;
    }

    if (root.hasLines()) {
      this.emitLines(out, root);
    } else {
      Block block = root.blocks;
      while (block != null) {
        this.emit(out, block);
        block = block.next;
      }
    }

    switch (root.type) {
      case NONE:
        break;
      case PARAGRAPH:
        this.config.decorator.closeParagraph(out);
        break;
      case BLOCKQUOTE:
        this.config.decorator.closeBlockquote(out);
        break;
      case FENCED_CODE:
        if (this.config.codeBlockEmitter == null) {
          this.config.decorator.closeCodeBlock(out);
        }
        break;
      case UNORDERED_LIST:
        this.config.decorator.closeUnorderedList(out);
        break;
      case ORDERED_LIST:
        this.config.decorator.closeOrderedList(out);
        break;
      case LIST_ITEM:
        this.config.decorator.closeListItem(out);
        break;
    }
  }

  /**
   * Transforms lines into HTML.
   *
   * @param out
   *     The StringBuilder to write to.
   * @param block
   *     The Block to process.
   */
  private void emitLines(final HtmlEscapeStringBuilder out, final Block block) {
    switch (block.type) {
      case FENCED_CODE:
        this.emitCodeLines(out, block.lines, block.meta);
        break;
      case PARAGRAPH:
        this.emitMarkedLines(out, block.lines);
        break;
      default:
        this.emitMarkedLines(out, block.lines);
        break;
    }
  }

  /**
   * Finds the position of the given Token in the given String.
   *
   * @param in
   *     The String to search on.
   * @param start
   *     The starting character position.
   * @param token
   *     The token to find.
   * @return The position of the token or -1 if none could be found.
   */
  private int findToken(final String in, int start, MarkToken token) {
    int pos = start;
    while (pos < in.length()) {
      if (this.getToken(in, pos) == token) {
        return pos;
      }
      pos++;
    }
    return -1;
  }

  /**
   * Checks if there is a valid markdown link definition.
   *
   * @param out
   *     The StringBuilder containing the generated output.
   * @param in
   *     Input String.
   * @param start
   *     Starting position.
   * @return The new position or -1 if there is no valid markdown link.
   */
  private int checkLink(final HtmlEscapeStringBuilder out, final String in, int start) {
    int pos = start + 1;
    final StringBuilder temp = new StringBuilder();

    temp.setLength(0);
    pos = Utils.readMdLinkId(temp, in, pos);
    if (pos < start) {
      return -1;
    }

    String name = temp.toString();
    LinkRef lr;
    final int oldPos = pos++;
    pos = Utils.skipSpaces(in, pos);
    if (pos < start) {
      lr = this.linkRefs.get(name.toLowerCase());
      if (lr != null) {
        pos = oldPos;
      } else {
        return -1;
      }
    } else if (in.charAt(pos) == '[') {
      pos++;
      temp.setLength(0);
      pos = Utils.readRawUntil(temp, in, pos, ']');
      if (pos < start) {
        return -1;
      }
      final String id = temp.length() > 0 ? temp.toString() : name;
      lr = this.linkRefs.get(id.toLowerCase());
    } else {
      lr = this.linkRefs.get(name.toLowerCase());
      if (lr != null) {
        pos = oldPos;
      } else {
        return -1;
      }
    }

    if (lr == null) {
      return -1;
    }
    if (lr.hasHttpScheme()) {
      this.config.decorator.openLink(out);
      out.appendHtml(" href=\"").append(lr.link).appendHtml("\" class=\"reference-link\">");
      this.recursiveEmitLine(out, name, 0, MarkToken.LINK);
      out.appendHtml("</a>");
    } else {
      out.appendHtml("<span class=\"reference-broken-link\">");
      this.recursiveEmitLine(out, name, 0, MarkToken.LINK);
      out.appendHtml("</span>");
    }
    out.appendHtml("<span class=\"reference-link-index\">")
        .append(lr.seqNumber)
        .appendHtml("</span>");

    return pos;
  }

  /**
   * Recursively scans through the given line, taking care of any markdown
   * stuff.
   *
   * @param out
   *     The StringBuilder to write to.
   * @param in
   *     Input String.
   * @param start
   *     Start position.
   * @param token
   *     The matching Token (for e.g. '*')
   * @return The position of the matching Token or -1 if token was NONE or no
   * Token could be found.
   */
  private int recursiveEmitLine(final HtmlEscapeStringBuilder out,
      final String in,
      int start,
      MarkToken token) {
    int pos = start, a, b;
    final HtmlEscapeStringBuilder temp = new HtmlEscapeStringBuilder();
    while (pos < in.length()) {

      final MarkToken mt = this.getToken(in, pos);
      if (token != MarkToken.NONE && (mt == token
          || token == MarkToken.EM_STAR && mt == MarkToken.STRONG_STAR
          || token == MarkToken.EM_UNDERSCORE && mt == MarkToken.STRONG_UNDERSCORE)) {
        return pos;
      }
      switch (mt) {
        case LINK:
          temp.reset();
          b = this.checkLink(temp, in, pos);
          if (b > 0) {
            out.appendHtml(temp);
            pos = b;
          } else {
            out.appendHtml(in.charAt(pos));
          }
          break;
        case EM_STAR:
        case EM_UNDERSCORE:
          temp.reset();
          b = this.recursiveEmitLine(temp, in, pos + 1, mt);
          if (b > 0) {
            this.config.decorator.openEmphasis(out);
            out.appendHtml(temp);
            this.config.decorator.closeEmphasis(out);
            pos = b;
          } else {
            out.appendHtml(in.charAt(pos));
          }
          break;
        case STRONG_STAR:
        case STRONG_UNDERSCORE:
          temp.reset();
          b = this.recursiveEmitLine(temp, in, pos + 2, mt);
          if (b > 0) {
            this.config.decorator.openStrong(out);
            out.appendHtml(temp);
            this.config.decorator.closeStrong(out);
            pos = b + 1;
          } else {
            out.appendHtml(in.charAt(pos));
          }
          break;
        case STRIKE:
          temp.reset();
          b = this.recursiveEmitLine(temp, in, pos + 2, mt);
          if (b > 0) {
            this.config.decorator.openStrike(out);
            out.appendHtml(temp);
            this.config.decorator.closeStrike(out);
            pos = b + 1;
          } else {
            out.appendHtml(in.charAt(pos));
          }
          break;
        case SUPER:
          temp.reset();
          b = this.recursiveEmitLine(temp, in, pos + 1, mt);
          if (b > 0) {
            this.config.decorator.openSuper(out);
            out.appendHtml(temp);
            this.config.decorator.closeSuper(out);
            pos = b;
          } else {
            out.appendHtml(in.charAt(pos));
          }
          break;
        case CODE_SINGLE:
        case CODE_DOUBLE:
          a = pos + (mt == MarkToken.CODE_DOUBLE ? 2 : 1);
          b = this.findToken(in, a, mt);
          if (b > 0) {
            pos = b + (mt == MarkToken.CODE_DOUBLE ? 1 : 0);
            while (a < b && in.charAt(a) == ' ') {
              a++;
            }
            if (a < b) {
              while (in.charAt(b - 1) == ' ') {
                b--;
              }
              this.config.decorator.openCodeSpan(out);
              out.appendHtml(in.substring(a, b));
              this.config.decorator.closeCodeSpan(out);
            }
          } else {
            out.appendHtml(in.charAt(pos));
          }
          break;
        case USER:
          if (token == MarkToken.LINK) {
            out.appendHtml(in.charAt(pos));
          } else {
            temp.reset();
            b = this.checkUserLink(temp, in, pos);
            if (b > 0) {
              out.appendHtml(temp);
              pos = b;
            } else {
              out.appendHtml(in.charAt(pos));
            }
          }
          break;
        case ZONE:
          if (token == MarkToken.LINK) {
            out.appendHtml(in.charAt(pos));
          } else {
            temp.reset();
            b = this.checkZoneLink(temp, in, pos);
            if (b > 0) {
              out.appendHtml(temp);
              pos = b;
            } else {
              out.appendHtml(in.charAt(pos));
            }
          }
          break;
        case ESCAPE:
          pos++;
          //$FALL-THROUGH$
        default:
          out.appendHtml(in.charAt(pos));
          break;
      }
      pos++;
    }
    return -1;
  }

  /**
   * change this should review Account.java
   *
   * @param out
   * @param in
   * @param start
   * @return
   */
  private int checkUserLink(HtmlEscapeStringBuilder out, String in, int start) {
    int pos = start + 3; // skip /u/
    StringBuilder temp = new StringBuilder();
    String targetString = in.substring(pos, Math.min(in.length(), pos + Account.NAME_MAX));
    for (int i = 0; i < targetString.length(); i++) {
      char c = targetString.charAt(i);
      if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_') {
        temp.append(c);
      } else {
        break;
      }
    }
    String username = temp.toString();
    if (username.length() < Account.NAME_MIN || username.equalsIgnoreCase("null")) {
      return -1;
    }

    this.config.decorator.openLink(out);
    out.appendHtml(" href=\"")
        .append("/u/")
        .append(username)
        .appendHtml("\" class=\"user-link\">")
        .append(username);
    out.appendHtml("</a>");
    return pos + username.length() - 1;
  }

  /**
   * change this should review Zone.java
   *
   * @param out
   * @param in
   * @param start
   * @return
   */
  private int checkZoneLink(HtmlEscapeStringBuilder out, String in, int start) {
    int pos = start + 3; // skip /z/
    StringBuilder temp = new StringBuilder();
    String targetString = in.substring(pos, Math.min(in.length(), pos + 20));
    boolean prevIsDash = false;
    for (int i = 0; i < targetString.length(); i++) {
      char c = targetString.charAt(i);
      if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
        temp.append(c);
        prevIsDash = false;
      } else if (c == '-') {
        if (i == 0) {
          return -1; //start with -
        }
        if (prevIsDash) {
          temp.deleteCharAt(temp.length() - 1); //remove last dash
          break;
        }
        temp.append(c);
        prevIsDash = true;
      } else {
        if (prevIsDash) {
          temp.deleteCharAt(temp.length() - 1); //remove last dash
        }
        break;
      }
    }
    String zone = temp.toString();
    if (zone.length() < 3 || zone.equalsIgnoreCase("null")) {
      return -1;
    }

    this.config.decorator.openLink(out);
    out.appendHtml(" href=\"")
        .append("/z/")
        .append(zone)
        .appendHtml("\" class=\"zone-link\">")
        .append(zone);
    out.appendHtml("</a>");
    return pos + zone.length() - 1;
  }

  /**
   * Check if there is any markdown Token.
   *
   * @param in
   *     Input String.
   * @param pos
   *     Starting position.
   * @return The Token.
   */
  private MarkToken getToken(final String in, final int pos) {
    final char c0 = pos > 0 ? whitespaceToSpace(in.charAt(pos - 1)) : ' ';
    final char c = whitespaceToSpace(in.charAt(pos));
    final char c1 = pos + 1 < in.length() ? whitespaceToSpace(in.charAt(pos + 1)) : ' ';
    final char c2 = pos + 2 < in.length() ? whitespaceToSpace(in.charAt(pos + 2)) : ' ';
    switch (c) {
      case '*':
        if (c1 == '*') {
          return c0 != ' ' || c2 != ' ' ? MarkToken.STRONG_STAR : MarkToken.EM_STAR;
        }
        return c0 != ' ' || c1 != ' ' ? MarkToken.EM_STAR : MarkToken.NONE;
      case '_':
        if (c1 == '_') {
          return c0 != ' ' || c2 != ' ' ? MarkToken.STRONG_UNDERSCORE : MarkToken.EM_UNDERSCORE;
        }
        return c0 != ' ' || c1 != ' ' ? MarkToken.EM_UNDERSCORE : MarkToken.NONE;
      case '~':
        if (c1 == '~') {
          return MarkToken.STRIKE;
        }
        return MarkToken.NONE;
      case '/':
        if (c1 == 'u' && c2 == '/') {
          return MarkToken.USER;
        }
        if (c1 == 'z' && c2 == '/') {
          return MarkToken.ZONE;
        }
        return MarkToken.NONE;
      case '[':
        return MarkToken.LINK;
      case ']':
        return MarkToken.NONE;
      case '`':
        return c1 == '`' ? MarkToken.CODE_DOUBLE : MarkToken.CODE_SINGLE;
      case '\\':
        switch (c1) {
          case '\\':
          case '[':
          case ']':
          case '(':
          case ')':
          case '{':
          case '}':
          case '#':
          case '"':
          case '\'':
          case '.':
          case '>':
          case '<':
          case '*':
          case '+':
          case '-':
          case '_':
          case '!':
          case '`':
          case '^':
            return MarkToken.ESCAPE;
          default:
            return MarkToken.NONE;
        }
      case '^':
        return c0 == '^' || c1 == '^' ? MarkToken.NONE : MarkToken.SUPER;
      default:
        return MarkToken.NONE;
    }
  }

  /**
   * Writes a set of markdown lines into the StringBuilder.
   *
   * @param out
   *     The StringBuilder to write to.
   * @param lines
   *     The lines to write.
   */
  private void emitMarkedLines(final HtmlEscapeStringBuilder out, final Line lines) {
    final HtmlEscapeStringBuilder in = new HtmlEscapeStringBuilder();
    Line line = lines;
    while (line != null) {
      if (!line.isEmpty) {
        in.append(line.value.substring(line.leading, line.value.length() - line.trailing));
        if (line.trailing >= 2) {
          in.appendHtml("<br>");
        }
      }
      if (line.next != null) {
        in.append('\n');
      }
      line = line.next;
    }
    this.recursiveEmitLine(out, in.toString(), 0, MarkToken.NONE);
  }

  /**
   * Writes a code block into the StringBuilder.
   *
   * @param out
   *     The StringBuilder to write to.
   * @param lines
   *     The lines to write.
   * @param meta
   *     Meta information.
   */
  private void emitCodeLines(final HtmlEscapeStringBuilder out,
      final Line lines,
      final String meta) {
    Line line = lines;
    final ArrayList<String> list = new ArrayList<>();
    while (line != null) {
      if (line.isEmpty) {
        list.add("");
      } else {
        list.add(line.value);
      }
      line = line.next;
    }
    this.config.codeBlockEmitter.emitBlock(out, list, meta);
  }

  public void emitRefLinks(final HtmlEscapeStringBuilder out) {
    if (linkRefs.isEmpty()) {
      return;
    }
    out.appendHtml("<div class=\"reference-appendix-block\">");
    linkRefs.forEach((s, linkRef) -> {
      out.appendHtml("<div class=\"reference-appendix-index\">")
          .append(linkRef.seqNumber)
          .appendHtml("</div>")
          .appendHtml("<div  class=\"reference-appendix-wrap\">");

      if (linkRef.hasHttpScheme()) {
        out.appendHtml("<a href=\"").append(linkRef.link).appendHtml("\"");
        if (!Strings.isNullOrEmpty(linkRef.title)) {
          out.appendHtml(" title=\"").append(linkRef.title).appendHtml("\"");
        }
        out.appendHtml(" rel=\"nofollow\" target=\"_blank\">")
            .append(linkRef.link)
            .appendHtml("</a>");
      } else {
        out.append(linkRef.link);
      }
      out.appendHtml("</div>\n");

    });
    out.appendHtml("</div>");
  }
}
