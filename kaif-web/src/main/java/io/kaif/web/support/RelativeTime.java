package io.kaif.web.support;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.TimeUnit;
import org.ocpsoft.prettytime.units.Day;

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
 * ${relativeTime(fooBean.createTime, "Day")} //second parameter is max time unit name
 */
public class RelativeTime implements TemplateMethodModelEx {

  @Override
  public Object exec(List arguments) throws TemplateModelException {
    if (arguments.size() < 1) {
      throw new TemplateModelException("require an Instant as argument");
    }
    if (arguments.size() > 2) {
      throw new TemplateModelException("too many arguments");
    }
    PrettyTime prettyTime = new PrettyTime(Environment.getCurrentEnvironment().getLocale());
    // only support day unit now
    if (arguments.size() == 2 && arguments.get(1).toString().equals("Day")) {
      List<TimeUnit> units = prettyTime.getUnits()
          .stream()
          .filter(timeUnit -> timeUnit.getMillisPerUnit() > new Day().getMillisPerUnit())
          .collect(Collectors.toList());
      units.forEach(prettyTime::removeUnit);
    }

    StringModel stringModel = (StringModel) arguments.get(0);
    Instant instant = (Instant) stringModel.getAdaptedObject(Instant.class);
    return prettyTime.format(Date.from(instant));
  }
}
