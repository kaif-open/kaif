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
 * Decorator interface.
 *
 * @author René Jeschke <rene_jeschke@yahoo.de>
 */
public interface Decorator {

  /**
   * Called when a paragraph is opened.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;p>");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void openParagraph(final HtmlEscapeStringBuilder out);

  /**
   * Called when a paragraph is closed.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;/p>\n");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void closeParagraph(final HtmlEscapeStringBuilder out);

  /**
   * Called when a code block is opened.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;pre>&lt;code>");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void openCodeBlock(final HtmlEscapeStringBuilder out);

  /**
   * Called when a code block is closed.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;/code>&lt;/pre>\n");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void closeCodeBlock(final HtmlEscapeStringBuilder out);

  /**
   * Called when a code span is opened.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;code>");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void openCodeSpan(final HtmlEscapeStringBuilder out);

  /**
   * Called when a code span is closed.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;/code>");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void closeCodeSpan(final HtmlEscapeStringBuilder out);

  /**
   * Called when a strong span is opened.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;strong>");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void openStrong(final HtmlEscapeStringBuilder out);

  /**
   * Called when a strong span is closed.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;/strong>");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void closeStrong(final HtmlEscapeStringBuilder out);

  /**
   * Called when a strike span is opened.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;s>");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void openStrike(final HtmlEscapeStringBuilder out);

  /**
   * Called when a strike span is closed.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;/s>");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void closeStrike(final HtmlEscapeStringBuilder out);

  /**
   * Called when an emphasis span is opened.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;em>");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void openEmphasis(final HtmlEscapeStringBuilder out);

  /**
   * Called when an emphasis span is closed.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;/em>");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void closeEmphasis(final HtmlEscapeStringBuilder out);

  /**
   * Called when a superscript span is opened.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;sup>");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void openSuper(final HtmlEscapeStringBuilder out);

  /**
   * Called when a superscript span is closed.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;/sup>");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void closeSuper(final HtmlEscapeStringBuilder out);

  /**
   * Called when an ordered list is opened.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;ol>\n");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void openOrderedList(final HtmlEscapeStringBuilder out);

  /**
   * Called when an ordered list is closed.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;/ol>\n");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void closeOrderedList(final HtmlEscapeStringBuilder out);

  /**
   * Called when an unordered list is opened.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;ul>\n");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void openUnorderedList(final HtmlEscapeStringBuilder out);

  /**
   * Called when an unordered list is closed.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;/ul>\n");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void closeUnorderedList(final HtmlEscapeStringBuilder out);

  /**
   * Called when a list item is opened.
   * <p>
   * <p>
   * <strong>Note:</strong> Don't close the HTML tag!
   * </p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;li");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void openListItem(final HtmlEscapeStringBuilder out);

  /**
   * Called when a list item is closed.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;/li>\n");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void closeListItem(final HtmlEscapeStringBuilder out);

  /**
   * Called when a link is opened.
   * <p>
   * <p>
   * <strong>Note:</strong> Don't close the HTML tag!
   * </p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;a");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void openLink(final HtmlEscapeStringBuilder out);

  /**
   * Called when a blockquote is opened.
   * <p>
   * Default implementation is:
   * <p>
   * <pre>
   * <code>out.append("&lt;blockquote>");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void openBlockquote(final HtmlEscapeStringBuilder out);

  /**
   * Called when a blockquote is closed.
   * <p>
   * <p>
   * Default implementation is:
   * </p>
   * <p>
   * <pre>
   * <code>out.append("&lt;/blockquote>\n");</code>
   * </pre>
   *
   * @param out The StringBuilder to write to.
   */
  public void closeBlockquote(final HtmlEscapeStringBuilder out);

}
