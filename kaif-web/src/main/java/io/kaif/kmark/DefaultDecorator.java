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
 *     public void openParagraph(StringBuilder out)
 *     {
 *         out.append("&lt;p class=\"myclass\">");
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
  public void openParagraph(StringBuilder out) {
    out.append("<p>");
  }

  @Override
  public void closeParagraph(StringBuilder out) {
    out.append("</p>\n");
  }

  @Override
  public void openCodeBlock(StringBuilder out) {
    out.append("<pre><code>");
  }

  @Override
  public void closeCodeBlock(StringBuilder out) {
    out.append("</code></pre>\n");
  }

  @Override
  public void openCodeSpan(StringBuilder out) {
    out.append("<code>");
  }

  @Override
  public void closeCodeSpan(StringBuilder out) {
    out.append("</code>");
  }

  @Override
  public void openStrong(StringBuilder out) {
    out.append("<strong>");
  }

  @Override
  public void closeStrong(StringBuilder out) {
    out.append("</strong>");
  }

  @Override
  public void openStrike(StringBuilder out) {
    out.append("<s>");
  }

  @Override
  public void closeStrike(StringBuilder out) {
    out.append("</s>");
  }

  @Override
  public void openEmphasis(StringBuilder out) {
    out.append("<em>");
  }

  @Override
  public void closeEmphasis(StringBuilder out) {
    out.append("</em>");
  }

  @Override
  public void openSuper(StringBuilder out) {
    out.append("<sup>");
  }

  @Override
  public void closeSuper(StringBuilder out) {
    out.append("</sup>");
  }

  @Override
  public void openOrderedList(StringBuilder out) {
    out.append("<ol>\n");
  }

  @Override
  public void closeOrderedList(StringBuilder out) {
    out.append("</ol>\n");
  }

  @Override
  public void openUnorderedList(StringBuilder out) {
    out.append("<ul>\n");
  }

  @Override
  public void closeUnorderedList(StringBuilder out) {
    out.append("</ul>\n");
  }

  @Override
  public void openListItem(StringBuilder out) {
    out.append("<li");
  }

  @Override
  public void closeListItem(StringBuilder out) {
    out.append("</li>\n");
  }

  @Override
  public void openLink(StringBuilder out) {
    out.append("<a");
  }

}
