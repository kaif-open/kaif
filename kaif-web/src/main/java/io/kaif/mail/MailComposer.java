package io.kaif.mail;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

import freemarker.template.Configuration;
import freemarker.template.SimpleScalar;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

@Component
public class MailComposer {

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private Configuration configuration;

  @Autowired
  private MailProperties mailProperties;

  public MailComposer() {
    //spring
  }

  public Mail createMail() {
    Mail mail = new Mail();
    mail.setFrom(mailProperties.getSenderAddress());
    mail.setFromName(mailProperties.getSenderName());
    return mail;
  }

  @VisibleForTesting
  MailComposer(MessageSource messageSource,
      Configuration configuration,
      MailProperties mailProperties) {
    this.messageSource = messageSource;
    this.configuration = configuration;
    this.mailProperties = mailProperties;
  }

  public String i18n(Locale locale, String key, Object... args) {
    return messageSource.getMessage(key, args, locale);
  }

  public String compose(Locale locale, String templateFile, Map<String, Object> model)
      throws MailException {
    try {
      Template template = configuration.getTemplate(templateFile, locale);
      StringWriter writer = new StringWriter();
      Map<String, Object> i18nModel = createI18nModel(locale);
      i18nModel.putAll(model);
      template.process(i18nModel, writer);
      writer.flush();
      return writer.toString();
    } catch (IOException e) {
      throw new MailParseException("unexpect IO error", e);
    } catch (TemplateException e) {
      throw new MailParseException("template error", e);
    }
  }

  /**
   * with FreemarkerMessageModel bind to <code>message</code>, the template can invoke method to
   * get
   * i18n:
   * <pre>
   *   ${message('mail_activation_hint')}
   * </pre>
   */
  private Map<String, Object> createI18nModel(Locale locale) {
    HashMap<String, Object> model = new HashMap<>();
    model.put("message", new FreemarkerMessageModel(messageSource, locale));
    return model;
  }

  /**
   * see http://stackoverflow.com/a/16572888 for how to manually use ResourceBundle in freemarker
   * template
   */
  public class FreemarkerMessageModel implements TemplateMethodModelEx {
    private final MessageSource messageSource;
    private final Locale locale;

    public FreemarkerMessageModel(MessageSource messageSource, Locale locale) {
      this.messageSource = messageSource;
      this.locale = locale;
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
      if (arguments.size() == 0) {
        throw new TemplateModelException("Wrong number of arguments");
      }
      SimpleScalar simpleScalar = (SimpleScalar) arguments.get(0);
      if (simpleScalar == null || Strings.isNullOrEmpty(simpleScalar.getAsString())) {
        throw new TemplateModelException("Invalid code value '" + simpleScalar + "'");
      }
      @SuppressWarnings("unchecked")
      Object[] args = arguments.stream().skip(1).map(Objects::toString).toArray(String[]::new);
      return messageSource.getMessage(simpleScalar.getAsString(), args, locale);
    }
  }
}
