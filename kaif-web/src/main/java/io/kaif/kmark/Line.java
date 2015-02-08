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

/**
 * This class represents a text line.
 * <p>
 * <p>
 * It also provides methods for processing and analyzing a line.
 * </p>
 *
 * @author René Jeschke <rene_jeschke@yahoo.de>
 */
class Line {

  /**
   * Current cursor position.
   */
  public int pos;
  /**
   * Leading and trailing spaces.
   */
  public int leading = 0, trailing = 0;
  /**
   * Is this line empty?
   */
  public boolean isEmpty = true;
  /**
   * This line's value.
   */
  public String value = null;
  /**
   * Previous and next line.
   */
  public Line previous = null, next = null;
  /**
   * Is previous/next line empty?
   */
  public boolean prevEmpty, nextEmpty;

  /**
   * Constructor.
   */
  public Line() {
    //
  }

  /**
   * Calculates leading and trailing spaces. Also sets empty if needed.
   */
  public void init() {
    this.leading = 0;
    while (this.leading < this.value.length() && this.value.charAt(this.leading) == ' ') {
      this.leading++;
    }

    if (this.leading == this.value.length()) {
      this.setEmpty();
    } else {
      this.isEmpty = false;
      this.trailing = 0;
      while (this.value.charAt(this.value.length() - this.trailing - 1) == ' ') {
        this.trailing++;
      }
    }
  }

  /**
   * Recalculate leading spaces.
   */
  public void initLeading() {
    this.leading = 0;
    while (this.leading < this.value.length() && this.value.charAt(this.leading) == ' ') {
      this.leading++;
    }

    if (this.leading == this.value.length()) {
      this.setEmpty();
    }
  }

  /**
   * Skips spaces.
   *
   * @return <code>false</code> if end of line is reached
   */
  // TODO use Util#skipSpaces
  public boolean skipSpaces() {
    while (this.pos < this.value.length() && this.value.charAt(this.pos) == ' ') {
      this.pos++;
    }
    return this.pos < this.value.length();
  }

  /**
   * Reads chars from this line until any 'end' char is reached.
   *
   * @param end Delimiting character(s)
   * @return The read String or <code>null</code> if no 'end' char was
   * reached.
   */
  // TODO use Util#readUntil
  public String readUntil(char... end) {
    final StringBuilder sb = new StringBuilder();
    int pos = this.pos;
    while (pos < this.value.length()) {
      final char ch = this.value.charAt(pos);
      if (ch == '\\' && pos + 1 < this.value.length()) {
        final char c;
        switch (c = this.value.charAt(pos + 1)) {
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
          case '*':
          case '+':
          case '-':
          case '_':
          case '!':
          case '`':
            sb.append(c);
            pos++;
            break;
          default:
            sb.append(ch);
            break;
        }
      } else {
        boolean endReached = false;
        for (final char anEnd : end) {
          if (ch == anEnd) {
            endReached = true;
            break;
          }
        }
        if (endReached) {
          break;
        }
        sb.append(ch);
      }
      pos++;
    }

    final char ch = pos < this.value.length() ? this.value.charAt(pos) : '\n';
    for (final char anEnd : end) {
      if (ch == anEnd) {
        this.pos = pos;
        return sb.toString();
      }
    }
    return null;
  }

  /**
   * Marks this line empty. Also sets previous/next line's empty attributes.
   */
  public void setEmpty() {
    this.value = "";
    this.leading = this.trailing = 0;
    this.isEmpty = true;
    if (this.previous != null) {
      this.previous.nextEmpty = true;
    }
    if (this.next != null) {
      this.next.prevEmpty = true;
    }
  }

  /**
   * Gets this line's type.
   *
   * @return The LineType.
   */
  public LineType getLineType() {
    if (this.isEmpty) {
      return LineType.EMPTY;
    }

    if (this.leading > 3) {
      return LineType.CODE;
    }
    if (this.value.length() - this.leading - this.trailing > 2 && (this.value.charAt(this.leading)
        == '`'
        || this.value.charAt(this.leading) == '~')) {
      if (this.countCharsStart('`') >= 3) {
        return LineType.FENCED_CODE;
      }

      if (this.countCharsStart('~') >= 3) {
        return LineType.FENCED_CODE;
      }

    }

    if (this.value.length() - this.leading >= 2 && this.value.charAt(this.leading + 1) == ' ') {
      switch (this.value.charAt(this.leading)) {
        case '*':
        case '-':
        case '+':
          return LineType.ULIST;
      }
    }

    if (this.value.length() - this.leading >= 3
        && Character.isDigit(this.value.charAt(this.leading))) {
      int i = this.leading + 1;
      while (i < this.value.length() && Character.isDigit(this.value.charAt(i))) {
        i++;
      }
      if (i + 1 < this.value.length()
          && this.value.charAt(i) == '.'
          && this.value.charAt(i + 1) == ' ') {
        return LineType.OLIST;
      }
    }

    return LineType.OTHER;
  }

  /**
   * Counts the amount of 'ch' at the start of this line ignoring spaces.
   *
   * @param ch The char to count.
   * @return Number of characters found.
   * @since 0.7
   */
  private int countCharsStart(char ch) {
    int count = 0;
    for (int i = 0; i < this.value.length(); i++) {
      final char c = this.value.charAt(i);
      if (c == ' ') {
        continue;
      }
      if (c == ch) {
        count++;
      } else {
        break;
      }
    }
    return count;
  }

  /**
   * Checks if this line contains an ID at it's end and removes it from the
   * line.
   *
   * @return The ID or <code>null</code> if no valid ID exists.
   */
  // FIXME ... hack
  public String stripID() {
    if (this.isEmpty || this.value.charAt(this.value.length() - this.trailing - 1) != '}') {
      return null;
    }
    int p = this.leading;
    boolean found = false;
    while (p < this.value.length() && !found) {
      switch (this.value.charAt(p)) {
        case '\\':
          if (p + 1 < this.value.length()) {
            switch (this.value.charAt(p + 1)) {
              case '{':
                p++;
                break;
            }
          }
          p++;
          break;
        case '{':
          found = true;
          break;
        default:
          p++;
          break;
      }
    }

    if (found) {
      if (p + 1 < this.value.length() && this.value.charAt(p + 1) == '#') {
        final int start = p + 2;
        p = start;
        found = false;
        while (p < this.value.length() && !found) {
          switch (this.value.charAt(p)) {
            case '\\':
              if (p + 1 < this.value.length()) {
                switch (this.value.charAt(p + 1)) {
                  case '}':
                    p++;
                    break;
                }
              }
              p++;
              break;
            case '}':
              found = true;
              break;
            default:
              p++;
              break;
          }
        }
        if (found) {
          final String id = this.value.substring(start, p).trim();
          if (this.leading != 0) {
            this.value = this.value.substring(0, this.leading)
                + this.value.substring(this.leading, start - 2).trim();
          } else {
            this.value = this.value.substring(this.leading, start - 2).trim();
          }
          this.trailing = 0;
          return id.length() > 0 ? id : null;
        }
      }
    }
    return null;
  }

}
