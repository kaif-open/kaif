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
 * Markdown token enumeration.
 *
 * @author René Jeschke <rene_jeschke@yahoo.de>
 */
enum MarkToken {
  /**
   * No token.
   */
  NONE,
  /**
   * &#x2a;
   */
  EM_STAR,            // x*x
  /**
   * _
   */
  EM_UNDERSCORE,      // x_x
  /**
   * &#x2a;&#x2a;
   */
  STRONG_STAR,        // x**x
  /**
   * __
   */
  STRONG_UNDERSCORE,  // x__x
  /**
   * ~~
   */
  STRIKE,             // x~~x
  /**
   * `
   */
  CODE_SINGLE,        // `
  /**
   * ``
   */
  CODE_DOUBLE,        // ``
  /**
   * [
   */
  LINK,               // [
  /**
   * \
   */
  ESCAPE,             // \x
  /**
   * Extended: ^
   */
  SUPER,              // ^
  /**
   * Extended: /u/NAME_PATTERN
   */
  USER,
  /**
   * Extended: /z/ZONE_PATTERN
   */
  ZONE,

  BR,
}
