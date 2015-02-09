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
 * Default Decorator implementation.
 * <p>
 * <p>
 * Example for a user Decorator having a class attribute on &lt;p> tags.
 * </p>
 * <p>
 * <pre>
 * <code>public class MyDecorator extends DefaultDecorator
 * {
 *     &#64;Override
 *     public void openParagraph(HtmlEscapeStringBuilder out)
 *     {
 *         out.appendHtml("&lt;p class=\"myclass\">");
 *     }
 * }
 * </code>
 * </pre>
 *
 * @author René Jeschke <rene_jeschke@yahoo.de>
 */
public class DefaultDecorator implements Decorator {

  /**
   * Constructor.
   */
  public DefaultDecorator() {
    // empty
  }

  @Override
  public void openParagraph(HtmlEscapeStringBuilder out) {
    out.appendHtml("<p>");
  }

  @Override
  public void closeParagraph(HtmlEscapeStringBuilder out) {
    out.appendHtml("</p>\n");
  }

  @Override
  public void openCodeBlock(HtmlEscapeStringBuilder out) {
    out.appendHtml("<pre><code>");
  }

  @Override
  public void closeCodeBlock(HtmlEscapeStringBuilder out) {
    out.appendHtml("</code></pre>\n");
  }

  @Override
  public void openCodeSpan(HtmlEscapeStringBuilder out) {
    out.appendHtml("<code>");
  }

  @Override
  public void closeCodeSpan(HtmlEscapeStringBuilder out) {
    out.appendHtml("</code>");
  }

  @Override
  public void openStrong(HtmlEscapeStringBuilder out) {
    out.appendHtml("<strong>");
  }

  @Override
  public void closeStrong(HtmlEscapeStringBuilder out) {
    out.appendHtml("</strong>");
  }

  @Override
  public void openStrike(HtmlEscapeStringBuilder out) {
    out.appendHtml("<s>");
  }

  @Override
  public void closeStrike(HtmlEscapeStringBuilder out) {
    out.appendHtml("</s>");
  }

  @Override
  public void openEmphasis(HtmlEscapeStringBuilder out) {
    out.appendHtml("<em>");
  }

  @Override
  public void closeEmphasis(HtmlEscapeStringBuilder out) {
    out.appendHtml("</em>");
  }

  @Override
  public void openSuper(HtmlEscapeStringBuilder out) {
    out.appendHtml("<sup>");
  }

  @Override
  public void closeSuper(HtmlEscapeStringBuilder out) {
    out.appendHtml("</sup>");
  }

  @Override
  public void openOrderedList(HtmlEscapeStringBuilder out) {
    out.appendHtml("<ol>\n");
  }

  @Override
  public void closeOrderedList(HtmlEscapeStringBuilder out) {
    out.appendHtml("</ol>\n");
  }

  @Override
  public void openUnorderedList(HtmlEscapeStringBuilder out) {
    out.appendHtml("<ul>\n");
  }

  @Override
  public void closeUnorderedList(HtmlEscapeStringBuilder out) {
    out.appendHtml("</ul>\n");
  }

  @Override
  public void openListItem(HtmlEscapeStringBuilder out) {
    out.appendHtml("<li");
  }

  @Override
  public void closeListItem(HtmlEscapeStringBuilder out) {
    out.appendHtml("</li>\n");
  }

  @Override
  public void openLink(HtmlEscapeStringBuilder out) {
    out.appendHtml("<a");
  }

  @Override
  public void openBlockquote(HtmlEscapeStringBuilder out) {
    out.appendHtml("<blockquote>");
  }

  @Override
  public void closeBlockquote(HtmlEscapeStringBuilder out) {
    out.appendHtml("</blockquote>\n");
  }

}
