package io.kaif.kmark;

import java.util.List;

public class CodeBlockEmitter implements BlockEmitter {

  @Override
  public void emitBlock(StringBuilder out, List<String> lines, String meta) {
    out.append("<pre><code");
    if (meta.length() > 0) {
      out.append(" class=\"" + meta + "\"");
    }
    out.append(">");
    for (final String s : lines) {
      for (int i = 0; i < s.length(); i++) {
        final char c = s.charAt(i);
        switch (c) {
          case '&':
            out.append("&amp;");
            break;
          case '<':
            out.append("&lt;");
            break;
          case '>':
            out.append("&gt;");
            break;
          default:
            out.append(c);
            break;
        }
      }
      out.append('\n');
    }
    out.append("</code></pre>\n");
  }

}
