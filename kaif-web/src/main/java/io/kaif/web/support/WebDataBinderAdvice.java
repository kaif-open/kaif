package io.kaif.web.support;

import java.beans.PropertyEditorSupport;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestController;

import io.kaif.flake.FlakeId;

@ControllerAdvice(annotations = { RestController.class, Controller.class })
public class WebDataBinderAdvice {
  public static class FlakeIdPropertyEditor extends PropertyEditorSupport {
    @Override
    public String getAsText() {
      FlakeId value = (FlakeId) getValue();
      return (value != null ? value.toString() : "");
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
      if (StringUtils.hasText(text)) {
        setValue(FlakeId.fromString(text));
      } else {
        setValue(null);
      }
    }
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(FlakeId.class, new WebDataBinderAdvice.FlakeIdPropertyEditor());
  }
}
