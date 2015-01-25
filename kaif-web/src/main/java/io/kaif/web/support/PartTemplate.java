package io.kaif.web.support;

import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.ImmutableMap;

public class PartTemplate {

  public static ModelAndView smallLayout() {
    return layout("small");
  }

  public static ModelAndView fullLayout() {
    return layout("full");
  }

  private static ModelAndView layout(String layoutName) {
    ModelAndView modelAndView = new ModelAndView("part-template");
    modelAndView.addAllObjects(ImmutableMap.of("part", ImmutableMap.of("layout", layoutName)));
    return modelAndView;
  }
}
