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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Markdown processor class.
 * <p>
 * <p>
 * Example usage:
 * </p>
 * <p>
 * <pre>
 * <code>String result = Processor.process("This is ***TXTMARK***");
 * </code>
 * </pre>
 *
 * @author René Jeschke <rene_jeschke@yahoo.de>
 */
public class Processor {

  /**
   * The reader.
   */
  private final Reader reader;
  /**
   * The emitter.
   */
  private final Emitter emitter;
  /**
   * The Configuration.
   */
  final Configuration config;

  /**
   * Constructor.
   *
   * @param reader The input reader.
   */
  protected Processor(final Reader reader, final Configuration config) {
    this.reader = reader;
    this.config = config;
    this.emitter = new Emitter(this.config);
  }

  /**
   * Transforms an input stream into HTML using the given Configuration.
   *
   * @param reader        The Reader to process.
   * @param configuration The Configuration.
   * @return The processed String.
   * @throws IOException if an IO error occurs
   * @see Configuration
   * @since 0.7
   */
  public static String process(final Reader reader, final Configuration configuration)
      throws IOException {
    final Processor p = new Processor(!(reader instanceof BufferedReader) ?
        new BufferedReader(reader) :
        reader,
        configuration);
    return p.process();
  }

  /**
   * Transforms an input String into HTML using the given Configuration.
   *
   * @param input         The String to process.
   * @param configuration The Configuration.
   * @return The processed String.
   * @see Configuration
   * @since 0.7
   */
  public static String process(final String input, final Configuration configuration) {
    try {
      return process(new StringReader(input), configuration);
    } catch (IOException e) {
      // This _can never_ happen
      return null;
    }
  }

  public static String process(final String input, final String linkAnchorPrefix) {
    return process(input,
        new Configuration.Builder().setLinkAnchorPrefix(linkAnchorPrefix).build());
  }

  /**
   * Reads all lines from our reader.
   * <p>
   * Takes care of markdown link references.
   * </p>
   *
   * @return A Block containing all lines.
   * @throws IOException If an IO error occurred.
   */
  private Block readLines() throws IOException {
    final Block block = new Block();
    final StringBuilder sb = new StringBuilder(200);
    int c = this.reader.read();
    LinkRef lastLinkRef = null;
    while (c != -1) {
      sb.setLength(0);
      int pos = 0;
      boolean eol = false;
      while (!eol) {
        switch (c) {
          case -1:
            eol = true;
            break;
          case '\n':
            c = this.reader.read();
            if (c == '\r') {
              c = this.reader.read();
            }
            eol = true;
            break;
          case '\r':
            c = this.reader.read();
            if (c == '\n') {
              c = this.reader.read();
            }
            eol = true;
            break;
          case '\t': {
            final int np = pos + (4 - (pos & 3));
            while (pos < np) {
              sb.append(' ');
              pos++;
            }
            c = this.reader.read();
            break;
          }
          default:
            pos++;
            sb.append((char) c);
            c = this.reader.read();
            break;
        }
      }

      final Line line = new Line();
      line.value = sb.toString();
      line.init();

      // Check for link definitions
      boolean isLinkRef = false;
      String id = null, link = null, comment = null;
      if (!line.isEmpty && line.leading < 4 && line.value.charAt(line.leading) == '[') {
        line.pos = line.leading + 1;
        // Read ID up to ']'
        id = line.readUntil(']');
        // Is ID valid and are there any more characters?
        if (id != null && line.pos + 2 < line.value.length()) {
          // Check for ':' ([...]:...)
          if (line.value.charAt(line.pos + 1) == ':') {
            line.pos += 2;
            line.skipSpaces();
            // Check for link syntax
            if (line.value.charAt(line.pos) == '<') {
              line.pos++;
              link = line.readUntil('>');
              line.pos++;
            } else {
              link = line.readUntil(' ', '\n');
            }

            // Is link valid?
            if (link != null) {
              // Any non-whitespace characters following?
              if (line.skipSpaces()) {
                final char ch = line.value.charAt(line.pos);
                // Read comment
                if (ch == '\"' || ch == '\'' || ch == '(') {
                  line.pos++;
                  comment = line.readUntil(ch == '(' ? ')' : ch);
                  // Valid linkRef only if comment is valid
                  if (comment != null) {
                    isLinkRef = true;
                  }
                }
              } else {
                isLinkRef = true;
              }
            }
          }
        }
      }

      if (isLinkRef) {
        // Store linkRef and skip line
        final LinkRef lr = this.emitter.addLinkRef(id, link, comment);
        if (comment == null) {
          lastLinkRef = lr;
        }
      } else {
        comment = null;
        // Check for multi-line linkRef
        if (!line.isEmpty && lastLinkRef != null) {
          line.pos = line.leading;
          final char ch = line.value.charAt(line.pos);
          if (ch == '\"' || ch == '\'' || ch == '(') {
            line.pos++;
            comment = line.readUntil(ch == '(' ? ')' : ch);
          }
          if (comment != null) {
            lastLinkRef.title = comment;
          }

          lastLinkRef = null;
        }

        // No multi-line linkRef, store line
        if (comment == null) {
          line.pos = 0;
          block.appendLine(line);
        }
      }
    }

    return block;
  }

  /**
   * Initializes a list block by separating it into list item blocks.
   *
   * @param root The Block to process.
   */
  private void initListBlock(final Block root) {
    Line line = root.lines;
    line = line.next;
    while (line != null) {
      final LineType t = line.getLineType();
      if ((t == LineType.OLIST || t == LineType.ULIST) || (!line.isEmpty && (line.prevEmpty
          && line.leading == 0))) {
        root.split(line.previous).type = BlockType.LIST_ITEM;
      }
      line = line.next;
    }
    root.split(root.lineTail).type = BlockType.LIST_ITEM;
  }

  /**
   * Recursively process the given Block.
   *
   * @param root     The Block to process.
   * @param listMode Flag indicating that we're in a list item block.
   */
  private void recurse(final Block root, boolean listMode) {
    Block block, list;
    Line line = root.lines;

    if (listMode) {
      root.removeListIndent();
    }

    while (line != null && line.isEmpty) {
      line = line.next;
    }
    if (line == null) {
      return;
    }

    while (line != null) {
      final LineType type = line.getLineType();
      switch (type) {
        case OTHER: {
          final boolean wasEmpty = line.prevEmpty;
          while (line != null && !line.isEmpty) {
            final LineType t = line.getLineType();
            if ((listMode) && (t == LineType.OLIST
                || t == LineType.ULIST)) {
              break;
            }
            if (t == LineType.FENCED_CODE || t == LineType.BQUOTE) {
              break;
            }
            line = line.next;
          }
          final BlockType bt;
          if (line != null && !line.isEmpty) {
            bt = (!wasEmpty) ? BlockType.NONE : BlockType.PARAGRAPH;
            root.split(line.previous).type = bt;
            root.removeLeadingEmptyLines();
          } else {
            bt = (listMode && (line == null || !line.isEmpty) && !wasEmpty) ? BlockType.NONE
                : BlockType.PARAGRAPH;
            root.split(line == null ? root.lineTail : line).type = bt;
            root.removeLeadingEmptyLines();
          }
          line = root.lines;
          break;
        }
        case BQUOTE:
          while (line != null) {
            if (!line.isEmpty
                && (line.prevEmpty
                && line.leading == 0
                && line.getLineType() != LineType.BQUOTE)) {
              break;
            }
            line = line.next;
          }
          block = root.split(line != null ? line.previous : root.lineTail);
          block.type = BlockType.BLOCKQUOTE;
          block.removeSurroundingEmptyLines();
          block.removeBlockQuotePrefix();
          this.recurse(block, false);
          line = root.lines;
          break;
        case FENCED_CODE:
          line = line.next;
          while (line != null) {
            if (line.getLineType() == LineType.FENCED_CODE) {
              break;
            }
            // TODO ... is this really necessary? Maybe add a special
            // flag?
            line = line.next;
          }
          if (line != null) {
            line = line.next;
          }
          block = root.split(line != null ? line.previous : root.lineTail);
          block.type = BlockType.FENCED_CODE;
          block.meta = Utils.getMetaFromFence(block.lines.value);
          block.lines.setEmpty();
          if (block.lineTail.getLineType() == LineType.FENCED_CODE) {
            block.lineTail.setEmpty();
          }
          block.removeSurroundingEmptyLines();
          break;
        case OLIST:
        case ULIST:
          while (line != null) {
            final LineType t = line.getLineType();
            if (!line.isEmpty
                && (line.prevEmpty && line.leading == 0 && !(t == LineType.OLIST
                || t == LineType.ULIST))) {
              break;
            }
            line = line.next;
          }
          list = root.split(line != null ? line.previous : root.lineTail);
          list.type = type == LineType.OLIST ? BlockType.ORDERED_LIST : BlockType.UNORDERED_LIST;
          list.lines.prevEmpty = false;
          list.lineTail.nextEmpty = false;
          list.removeSurroundingEmptyLines();
          list.lines.prevEmpty = list.lineTail.nextEmpty = false;
          initListBlock(list);
          block = list.blocks;
          while (block != null) {
            this.recurse(block, true);
            block = block.next;
          }
          list.expandListParagraphs();
          break;
        default:
          line = line.next;
          break;
      }
    }
  }

  /**
   * Does all the processing.
   *
   * @return The processed String.
   * @throws IOException If an IO error occurred.
   */
  private String process() throws IOException {
    final HtmlEscapeStringBuilder out = new HtmlEscapeStringBuilder();
    final Block parent = this.readLines();
    parent.removeSurroundingEmptyLines();
    this.recurse(parent, false);
    Block block = parent.blocks;
    while (block != null) {
      this.emitter.emit(out, block);
      block = block.next;
    }
    this.emitter.emitRefLinks(out);
    return out.toString();
  }
}
