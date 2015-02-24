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
 * A markdown link reference.
 *
 * @author René Jeschke <rene_jeschke@yahoo.de>
 */
class LinkRef {

  /**
   * reference sequence
   */
  public final int seqNumber;

  /**
   * The link.
   */
  public final String link;
  /**
   * The optional comment/title.
   */
  public String title;

  /**
   * Constructor.
   *
   * @param link
   *     The link.
   * @param title
   *     The title (may be <code>null</code>).
   */
  public LinkRef(final int seqNumber, final String link, final String title) {
    this.seqNumber = seqNumber;
    this.link = link;
    this.title = title;
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    return this.link + " \"" + this.title + "\"";
  }

  public boolean hasHttpScheme() {
    return link.startsWith("http://") || link.startsWith("https://");
  }
}
