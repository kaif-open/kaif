package io.kaif.web.support;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import freemarker.core.Environment;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * to display absolute time based on java 8 Instant.
 * <p>
 * for example, to display
 * <p>
 * Instant time = fooBean.getCreateTime()
 * <p>
 * use:
 * <p>
 * ${absoluteTime(fooBean.createTime)}
 */
public class AbsoluteTime implements TemplateMethodModelEx {

  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");

  @Override
  public Object exec(List arguments) throws TemplateModelException {
    if (arguments.size() != 1) {
      throw new TemplateModelException("require an Instant as argument");
    }
    StringModel stringModel = (StringModel) arguments.get(0);
    Instant instant = (Instant) stringModel.getAdaptedObject(Instant.class);
    ZoneId zoneId = Environment.getCurrentEnvironment().getTimeZone().toZoneId();
    return formatter.withLocale(Environment.getCurrentEnvironment().getLocale())
        .withZone(zoneId)
        .format(instant);
  }
}
