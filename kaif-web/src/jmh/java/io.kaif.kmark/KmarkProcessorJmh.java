package io.kaif.kmark;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Thread)
public class KmarkProcessorJmh {

  private String text =
      "I'm referring to this code on the originally linked page [1][1] (you'll have to scroll back a bit because your header blocks the content).\n"
          + "In the context of this thread, brghts states that this is dangerous because if you compile with -DNDEBUG the assert is optimized away.\n"
          + "So if I copy that code with the assert statement, it will be optimized away and your code no longer performs the NULL check. This is bad.\n"
          + "As you *mention*, beginners tend to copy code off the Internet and cause bugs. If you recognize this and claim to be teaching people you should not *use* bad practices in your example code. Period.\n"
          + "If you don't want to __muddy__ the waters with your custom debug macros, then you should still play it safe when checking return values the a beginner may simply copy and think is correct.\n"
          + "\n"
          + "```\n"
          + "public class KmarkProcessorJmh {\n"
          + "\n"
          + "  @Benchmark\n"
          + "  public void process_normal() {\n"
          + "\n"
          + "  }\n"
          + "\n"
          + "  public static void main(String[] args) throws Exception {\n"
          + "    Options opt = new OptionsBuilder().include(KmarkProcessorJmh.class.getSimpleName())\n"
          + "        .forks(1)\n"
          + "        .build();\n"
          + "\n"
          + "    new Runner(opt).run();\n"
          + "  }\n"
          + "}\n"
          + "```\n"
          + "\n"
          + "> 12  \n"
          + "> 456\n"
          + "> 789\n"
          + "> I'm referring to this code on the originally linked page [1][1] (you'll have to scroll back a bit because your header blocks the content).  \n"
          + "\n"
          + "\n"
          + "I'm referring to this code on the originally linked page [1][1] (you'll have to scroll back a bit because your header blocks the content).  \n"
          + "In the context of this thread, brghts states[2][2] that this is dangerous because if you compile with -DNDEBUG the assert is optimized away.\n"
          + "So if I copy that code with the assert statement[3][3], it will be optimized away and your code no longer performs the NULL check. This is bad.\n"
          + "\n"
          + "\n"
          + "* good\n"
          + "* bad\n"
          + "\n"
          + "\n"
          + "* As you mention, beginners tend to copy code off the Internet and cause bugs. If you recognize this and claim to be teaching \n"
          + "people you should not use bad practices in your example code. Period.\n"
          + "\n"
          + "* If you don't want to muddy the waters with your custom debug macros, then you should still play it safe when checking return values the a beginner may simply copy and think is correct.\n"
          + "\n"
          + "\n"
          + "\n"
          + "[1]: http://c.learncodethehardway.org/book/krcritique.html#code--...\n"
          + "[2]: http://c.learncodethehardway.org/book/krcritique.html#code--...\n"
          + "[3]: http://c.learncodethehardway.org/book/krcritique.html#code--...";

  private String anchorPrefix = "prefix";

  @Benchmark
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  @BenchmarkMode({ Mode.AverageTime, Mode.SampleTime, Mode.SingleShotTime })
  public String process_normal() {
    return new KmarkProcessor().process(text, anchorPrefix);
  }

  public static void main(String[] args) throws Exception {
    Options opt = new OptionsBuilder().include(KmarkProcessorJmh.class.getSimpleName())
        .forks(1)
        .build();

    new Runner(opt).run();
  }

}