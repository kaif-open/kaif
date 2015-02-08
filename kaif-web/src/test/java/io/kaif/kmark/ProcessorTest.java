package io.kaif.kmark;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ProcessorTest {

  @Test
  public void process() throws Exception {
    assertEquals(readTestFile("kmark/out1.out"), Processor.process(readTestFile("kmark/in1.md")));
    assertEquals(readTestFile("kmark/out2.out"), Processor.process(readTestFile("kmark/in2.md")));
  }

  static String readTestFile(String fileName) throws IOException {
    try (java.util.Scanner s = new java.util.Scanner(new ClassPathResource(fileName)
        .getInputStream()
    )) {
      return s.useDelimiter("\\A").hasNext() ? s.next() : "";
    }
  }
}