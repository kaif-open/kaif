package io.kaif.kmark;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ProcessorTest {

  @Test
  public void process_emphasis() throws Exception {
    assertEquals(readTestFile("kmark/out1.out"),
        Processor.process(readTestFile("kmark/in1.md"), ""));
  }

  @Test
  public void process_quote() throws Exception {
    assertEquals(readTestFile("kmark/out9.out"),
        Processor.process(readTestFile("kmark/in9.md"), ""));
  }
  
  @Test
  public void process_list() throws Exception {
    assertEquals(readTestFile("kmark/out5.out"),
        Processor.process(readTestFile("kmark/in5.md"), ""));
  }

  @Test
  public void process_fence_code() throws Exception {
    assertEquals(readTestFile("kmark/out2.out"),
        Processor.process(readTestFile("kmark/in2.md"), ""));
  }

  @Test
  public void process_ignore_new_lines() throws Exception {
    assertEquals(readTestFile("kmark/out8.out"),
        Processor.process(readTestFile("kmark/in8.md"), ""));
  }

  @Test
  public void process_ignore_legacy_code() throws Exception {
    assertEquals(readTestFile("kmark/out6.out"),
        Processor.process(readTestFile("kmark/in6.md"), ""));
  }

  @Test
  public void process_ignore_inline_link() throws Exception {
    assertEquals(readTestFile("kmark/out7.out"),
        Processor.process(readTestFile("kmark/in7.md"), ""));
  }

  @Test
  public void process_reference_link() throws Exception {
    assertEquals(readTestFile("kmark/out3.out"),
        Processor.process(readTestFile("kmark/in3.md"), "aAbBz"));
  }

  @Test
  public void process_escape_html() throws Exception {
    assertEquals(readTestFile("kmark/out4.out"),
        Processor.process(readTestFile("kmark/in4.md"), ""));
  }

  static String readTestFile(String fileName) throws IOException {
    try (java.util.Scanner s = new java.util.Scanner(new ClassPathResource(fileName)
        .getInputStream()
    )) {
      return s.useDelimiter("\\A").hasNext() ? s.next() : "";
    }
  }
}