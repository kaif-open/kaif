package io.kaif.web;

import java.io.IOException;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.service.ZoneService;

@Controller
@RequestMapping("/z")
public class ZoneController {

  @Autowired
  private ZoneService zoneService;

  @RequestMapping("/{zone}")
  public Object hotArticles(@PathVariable("zone") String rawZone, HttpServletRequest request)
      throws IOException {
    return resolveZone(request, rawZone, zoneInfo -> {
      return new ModelAndView("zone/articles").addObject("zoneInfo", zoneInfo);
    });
  }

  private Object resolveZone(HttpServletRequest request,
      String decodedRawZone,
      Function<ZoneInfo, ModelAndView> onZoneInfo) {
    // note that decodedRawZone already do http url decode, and PathVariable already trim()
    // space of value
    return Zone.tryFallback(decodedRawZone).map(zone -> {
      if (!zone.value().equals(decodedRawZone)) {
        String orgUrl = request.getRequestURL().toString();
        // replace pattern is combine of fallback pattern and valid pattern
        // TODO refactor replace rule to Zone
        String location = orgUrl.replaceFirst("/z/[a-zA-Z0-9_\\-]+", "/z/" + zone);
        //check if fallback success, this prevent infinite redirect loop
        if (!location.equals(orgUrl)) {
          RedirectView redirectView = new RedirectView(location);
          redirectView.setPropagateQueryParams(true);
          redirectView.setExpandUriTemplateVariables(false);
          redirectView.setExposeModelAttributes(false);
          redirectView.setExposeContextBeansAsAttributes(false);
          redirectView.setExposePathVariables(false);
          redirectView.setContextRelative(true);
          redirectView.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
          return redirectView;
        }
      }
      return onZoneInfo.apply(zoneService.getZone(zone));
    }).orElseThrow(() -> new EmptyResultDataAccessException("no such zone: " + decodedRawZone, 1));

  }

  @RequestMapping("/{zone}/new")
  public Object newArticles(@PathVariable("zone") String rawZone, HttpServletRequest request) {
    return resolveZone(request, rawZone, zoneInfo -> {
      return new ModelAndView("zone/articles").addObject("zoneInfo", zoneInfo);
    });
  }
}
