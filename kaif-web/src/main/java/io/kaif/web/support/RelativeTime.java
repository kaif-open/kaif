package io.kaif.web.support;

import java.sql.Date;
import java.time.Instant;
import java.util.List;

import org.ocpsoft.prettytime.PrettyTime;

import freemarker.core.Environment;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * to display relative time based on java 8 Instant.
 * <p>
 * for example, to display
 * <p>
 * Instant time = fooBean.getCreateTime()
 * <p>
 * use:
 * <p>
 * ${relativeTime(fooBean.createTime)}
 */
public class RelativeTime implements TemplateMethodModelEx {

  @Override
  public Object exec(List arguments) throws TemplateModelException {
    if (arguments.size() != 1) {
      throw new TemplateModelException("require an Instant as argument");
    }
    StringModel stringModel = (StringModel) arguments.get(0);
    Instant instant = (Instant) stringModel.getAdaptedObject(Instant.class);
    return new PrettyTime(Environment.getCurrentEnvironment()
        .getLocale()).format(Date.from(instant));
  }
}
