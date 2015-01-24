package io.kaif.mail;

import org.junit.Before;
import org.springframework.context.support.ResourceBundleMessageSource;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;

/**
 * Created by ingram on 1/25/15.
 */
public class MailTestCase {
  protected Configuration configuration;
  protected ResourceBundleMessageSource messageSource;

  @Before
  public void mailSetup() {
    configuration = new Configuration(Configuration.VERSION_2_3_21);
    configuration.setDefaultEncoding("UTF-8");
    configuration.setTemplateLoader(new ClassTemplateLoader(MailComposer.class, "/mail"));
    messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename("i18n/messages");
    messageSource.setDefaultEncoding("UTF-8");
  }
}
