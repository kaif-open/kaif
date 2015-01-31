package io.kaif.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import io.kaif.model.ZoneService;

@Controller
@RequestMapping("/z")
public class ZoneController {

  @Autowired
  private ZoneService zoneService;

  @RequestMapping("/{zone}")
  public ModelAndView hotArticles(@PathVariable("zone") String zone) {
    return new ModelAndView("zone/articles").addObject("zoneInfo", zoneService.getZone(zone));
  }

  @RequestMapping("/{zone}/new")
  public ModelAndView newArticles(@PathVariable("zone") String zone) {
    return new ModelAndView("zone/articles").addObject("zoneInfo", zoneService.getZone(zone));
  }
}
