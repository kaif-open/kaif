package io.kaif.mail;

import static org.junit.Assert.*;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class MailComposerTest extends MailTestCase {

  private MailComposer composer;

  @Before
  public void setUp() throws Exception {
    composer = new MailComposer(messageSource, configuration, new MailProperties());
  }

  @Test
  public void i18n() throws Exception {
    assertEquals("測試中", composer.i18n(Locale.TAIWAN, "unit-test"));
    assertEquals("testing", composer.i18n(Locale.ENGLISH, "unit-test"));
    assertEquals("testing", composer.i18n(Locale.FRENCH, "unit-test"));
  }

  @Test
  public void compose() throws Exception {
    String templateFile = "/mail-composer-test.ftl";
    ImmutableMap<String, Object> model = ImmutableMap.of("foo", "1", "bar", "2");
    String composed = composer.compose(Locale.TAIWAN, templateFile, model);
    assertEquals("測試中\n" + "foo:1\n" + "bar:2", composed);
    composed = composer.compose(Locale.ENGLISH, templateFile, model);
    assertEquals("testing\n" + "foo:1\n" + "bar:2", composed);
  }
}