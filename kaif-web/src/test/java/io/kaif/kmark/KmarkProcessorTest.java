package io.kaif.kmark;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class KmarkProcessorTest {

  @Before
  public void setup() {
  }

  /**
   * ~~aaa~~
   * ///////////////////////////
   * <del>aaa</del>
   *
   * @throws Exception
   */
  @Test
  public void process_strike_through() throws Exception {
    assertEquals(readTestFile("kmark/out12.out"),
        KmarkProcessor.process(readTestFile("kmark/in12.md")));
  }

  /**
   * *Italic*, **bold**, `monospace`, 2^3^
   * ///////////////////////////
   * <em>Italic</em>, <strong>bold</strong>, <code>monospace</code>, 2<sup>3</sup>
   *
   * @throws Exception
   */
  @Test
  public void process_emphasis() throws Exception {
    assertEquals(readTestFile("kmark/out1.out"),
        KmarkProcessor.process(readTestFile("kmark/in1.md")));
  }

  /**
   * /u/koji
   * ///////////////////////////
   * <a href="/u/koji">koji</a>
   *
   * @throws Exception
   */
  @Test
  public void process_user_link() throws Exception {
    assertEquals(readTestFile("kmark/out13.out"),
        KmarkProcessor.process(readTestFile("kmark/in13.md")));
  }

  /**
   * /z/programming
   * ///////////////////////////
   * <a href="/z/programming">programming</a>
   *
   * @throws Exception
   */
  @Test
  public void process_zone_link() throws Exception {
    assertEquals(readTestFile("kmark/out14.out"),
        KmarkProcessor.process(readTestFile("kmark/in14.md")));
  }

  /**
   * a(_space_)(_space_)
   * b
   * ///////////////////////////
   * a<br>
   * b
   *
   * @throws Exception
   */
  @Test
  public void process_2_space_for_new_line() throws Exception {
    assertEquals(readTestFile("kmark/out11.out"),
        KmarkProcessor.process(readTestFile("kmark/in11.md")));
  }

  /**
   * > abc
   * > def
   * ///////////////////////////
   * <blockquote><p>abc
   * def</p></blockquote>
   *
   * @throws Exception
   */
  @Test
  public void process_quote() throws Exception {
    assertEquals(readTestFile("kmark/out9.out"),
        KmarkProcessor.process(readTestFile("kmark/in9.md")));
  }

  /**
   * * a
   * * b
   * * c
   * \n
   * - a
   * - b
   * - c
   * \n
   * + a
   * + b
   * + c
   * \n
   * 1. a
   * 2. b
   * 5. c
   * -----------------------------------------
   * <ul>
   * <li>a</li>
   * <li>b</li>
   * <li>c</li>
   * </ul>
   *
   * @throws Exception
   */
  @Test
  public void process_list() throws Exception {
    assertEquals(readTestFile("kmark/out5.out"),
        KmarkProcessor.process(readTestFile("kmark/in5.md")));
  }

  /**
   * ```
   * class A...
   * ```
   * -----------------------------------------
   * <pre><code>class A...</code></pre>
   *
   * @throws Exception
   */
  @Test
  public void process_fence_code() throws Exception {
    assertEquals(readTestFile("kmark/out2.out"),
        KmarkProcessor.process(readTestFile("kmark/in2.md")));
  }

  /**
   * a\n
   * \n
   * \n
   * \n
   * b
   * -----------------------------------------
   * <p>a</p>
   * <p>b</p>
   *
   * @throws Exception
   */
  @Test
  public void process_ignore_new_lines() throws Exception {
    assertEquals(readTestFile("kmark/out8.out"),
        KmarkProcessor.process(readTestFile("kmark/in8.md")));
  }

  /**
   * println
   * -----------------------------------------
   * <p>println</p>
   *
   * @throws Exception
   */
  @Test
  public void process_ignore_legacy_code() throws Exception {
    assertEquals(readTestFile("kmark/out6.out"),
        KmarkProcessor.process(readTestFile("kmark/in6.md")));
  }

  /**
   * [This link](http://example.net/)
   * -----------------------------------------
   * <p>[This link](http://example.net/)</p>
   *
   * @throws Exception
   */
  @Test
  public void process_ignore_inline_link() throws Exception {
    assertEquals(readTestFile("kmark/out7.out"),
        KmarkProcessor.process(readTestFile("kmark/in7.md")));
  }

  /**
   * hello world [yahoo][1], [google][2], [likName][], [example][], [bar][4]
   * \n
   * [1]:http://yahoo.com.tw
   * [2]:http://google.com
   * [likName]:http://foo.com
   * [4]:http://bar.com "site for bar"
   * [example]:https://example.com
   * -----------------------------------------
   * <p>hello world <a href="#aAbBz-1">yahoo</a>, <a href="#aAbBz-2">google</a>, <a
   * href="#aAbBz-3">likName</a>, <a href="#aAbBz-5">example</a>, <a href="#aAbBz-4">bar</a></p>
   * <p>[1] <a href="http://yahoo.com.tw" >http://yahoo.com.tw</a><br>
   * [2] <a href="http://google.com" >http://google.com</a><br>
   * [3] <a href="http://foo.com" >http://foo.com</a><br>
   * [4] <a href="http://bar.com" >http://bar.com</a><br>
   * [5] <a href="https://example.com" >https://example.com</a><br>
   * </p>
   * <p>
   * <p>[This link](http://example.net/)</p>
   *
   * @throws Exception
   */
  @Test
  public void process_reference_link() throws Exception {
    assertEquals(readTestFile("kmark/out3.out"),
        KmarkProcessor.process(readTestFile("kmark/in3.md")));
  }

  /**
   * <span attr="evil">the</span> <script>no!!</script>
   * -----------------------------------------
   * &lt;span attr=&quot;evil&quot;&gt;the&lt;/span&gt; &lt;script&gt;no!!&lt;/script&gt;
   *
   * @throws Exception
   */
  @Test
  public void process_escape_html() throws Exception {
    assertEquals(readTestFile("kmark/out4.out"),
        KmarkProcessor.process(readTestFile("kmark/in4.md")));
  }

  @Test
  public void process_with_surrogate_character() throws Exception {
    assertEquals(readTestFile("kmark/out10.out"),
        KmarkProcessor.process(readTestFile("kmark/in10.md")));
  }

  static String readTestFile(String fileName) throws IOException {
    try (java.util.Scanner s = new java.util.Scanner(new ClassPathResource(fileName).getInputStream())) {
      return s.useDelimiter("\\A").hasNext() ? s.next() : "";
    }
  }

}