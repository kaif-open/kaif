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
 * Txtmark configuration.
 *
 * @author René Jeschke <rene_jeschke@yahoo.de>
 * @since 0.7
 */
public class Configuration {

  final String encoding;
  final Decorator decorator;
  final BlockEmitter codeBlockEmitter;
  final String linkAnchorPrefix;

  Configuration(
      String encoding,
      Decorator decorator,
      BlockEmitter codeBlockEmitter,
      String linkAnchorPrefix
  ) {
    this.encoding = encoding;
    this.decorator = decorator;
    this.codeBlockEmitter = codeBlockEmitter;
    this.linkAnchorPrefix = linkAnchorPrefix;
  }

  /**
   * Creates a new Builder instance.
   *
   * @return A new Builder instance.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Configuration builder.
   *
   * @author René Jeschke <rene_jeschke@yahoo.de>
   * @since 0.7
   */
  public static class Builder {

    private String encoding = "UTF-8";
    private Decorator decorator = new DefaultDecorator();
    private BlockEmitter codeBlockEmitter = new CodeBlockEmitter();
    private String linkAnchorPrefix = "";

    /**
     * Constructor.
     */
    Builder() {
      // empty
    }

    /**
     * Sets the character encoding for txtmark.
     * <p>
     * Default: <code>&quot;UTF-8&quot;</code>
     *
     * @param encoding The encoding
     * @return This builder
     * @since 0.7
     */
    public Builder setEncoding(String encoding) {
      this.encoding = encoding;
      return this;
    }

    /**
     * Sets the decorator for txtmark.
     * <p>
     * Default: <code>DefaultDecorator()</code>
     *
     * @param decorator The decorator
     * @return This builder
     * @see DefaultDecorator
     * @since 0.7
     */
    public Builder setDecorator(Decorator decorator) {
      this.decorator = decorator;
      return this;
    }

    /**
     * Sets the code block emitter.
     * <p>
     * Default: <code>null</code>
     *
     * @param emitter The BlockEmitter
     * @return This builder
     * @see BlockEmitter
     * @since 0.7
     */
    public Builder setCodeBlockEmitter(BlockEmitter emitter) {
      this.codeBlockEmitter = emitter;
      return this;
    }

    /**
     * will append to kmark's ref link
     * eg. aaa will become #aaa-1, #aaa-2, #aaa-3
     *
     * @param linkAnchorPrefix
     * @return
     */
    public Builder setLinkAnchorPrefix(String linkAnchorPrefix) {
      this.linkAnchorPrefix = linkAnchorPrefix;
      return this;
    }

    /**
     * Builds a configuration instance.
     *
     * @return a Configuration instance
     * @since 0.7
     */
    public Configuration build() {
      return new Configuration(this.encoding,
          this.decorator,
          this.codeBlockEmitter,
          this.linkAnchorPrefix);
    }

    public Decorator getDecorator() {
      return decorator;
    }
  }
}
