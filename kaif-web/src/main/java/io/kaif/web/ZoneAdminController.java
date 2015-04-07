package io.kaif.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import io.kaif.model.zone.Zone;

@Controller
@RequestMapping("/zone")
public class ZoneAdminController {

  @RequestMapping("/create")
  public ModelAndView createZone() {
    return new ModelAndView("zone/create").addObject("zonePattern", Zone.ZONE_PATTERN_STR);
  }
}
