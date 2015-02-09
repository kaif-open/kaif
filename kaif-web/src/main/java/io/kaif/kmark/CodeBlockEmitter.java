package io.kaif.kmark;

import java.util.List;

public class CodeBlockEmitter implements BlockEmitter {

  @Override
  public void emitBlock(HtmlEscapeStringBuilder out, List<String> lines, String meta) {
    out.appendHtml("<pre><code");
    if (meta.length() > 0) {
      out.appendHtml(" class=\"").append(meta).appendHtml("\"");
    }
    out.appendHtml(">");
    lines.stream()
        .map(s -> s + '\n')
        .forEach(out::append);
    out.appendHtml("</code></pre>\n");
  }

}
