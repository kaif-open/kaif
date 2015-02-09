package io.kaif.kmark;

import org.springframework.web.util.HtmlUtils;

public class HtmlEscapeStringBuilder {

  private StringBuilder stringBuilder;

  public HtmlEscapeStringBuilder() {
    this.stringBuilder = new StringBuilder();
  }

  public HtmlEscapeStringBuilder appendHtml(String html) {
    this.stringBuilder.append(html);
    return this;
  }
  public HtmlEscapeStringBuilder appendHtml(char c) {
    this.stringBuilder.append(Character.toString(c));
    return this;
  }

  public HtmlEscapeStringBuilder append(String text) {
    this.stringBuilder.append(HtmlUtils.htmlEscape(text));
    return this;
  }

  public HtmlEscapeStringBuilder append(HtmlEscapeStringBuilder builder) {
    this.stringBuilder.append(builder.stringBuilder);
    return this;
  }

  @Override public String toString() {
    return stringBuilder.toString();
  }

  public HtmlEscapeStringBuilder append(final int number) {
    stringBuilder.append(number);
    return this;
  }

  public HtmlEscapeStringBuilder append(final char c) {
    stringBuilder.append(HtmlUtils.htmlEscape(Character.toString(c)));
    return this;
  }

  public void setLength(final int length) {
    stringBuilder.setLength(length);
  }

}
