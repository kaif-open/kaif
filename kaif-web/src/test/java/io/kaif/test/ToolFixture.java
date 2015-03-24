package io.kaif.test;

public interface ToolFixture {

  default String q(String singleQuoted) {
    return singleQuoted.replaceAll("'", "\"");
  }
}
