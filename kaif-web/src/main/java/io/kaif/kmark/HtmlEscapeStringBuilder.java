package io.kaif.kmark;

import org.springframework.web.util.HtmlUtils;

public class HtmlEscapeStringBuilder {

  private StringBuilder stringBuilder;

  public HtmlEscapeStringBuilder() {
    this.stringBuilder = new StringBuilder();
  }

  public HtmlEscapeStringBuilder appendHtml(String htmlText) {
    this.stringBuilder.append(htmlText);
    return this;
  }

  public HtmlEscapeStringBuilder appendHtml(char htmlChar) {
    this.stringBuilder.append(Character.toString(htmlChar));
    return this;
  }

  public HtmlEscapeStringBuilder append(String unsafeText) {
    this.stringBuilder.append(HtmlUtils.htmlEscape(unsafeText));
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

  public HtmlEscapeStringBuilder append(final char unsafeChar) {
    stringBuilder.append(HtmlUtils.htmlEscape(Character.toString(unsafeChar)));
    return this;
  }

  public void reset() {
    stringBuilder.setLength(0);
  }

}
