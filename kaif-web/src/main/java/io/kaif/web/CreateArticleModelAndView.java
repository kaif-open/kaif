package io.kaif.web;

import static java.util.stream.Collectors.*;

import javax.annotation.Nullable;

import org.springframework.web.servlet.ModelAndView;

import io.kaif.model.zone.ZoneInfo;
import io.kaif.service.ZoneService;

public class CreateArticleModelAndView extends ModelAndView {
  public CreateArticleModelAndView(ZoneService zoneService, @Nullable ZoneInfo zoneInfo) {
    super("article/create");
    addObject("zoneInfo", zoneInfo);
    addObject("candidateZoneInfos",
        zoneService.listRecommendZones()
            .stream()
            .filter(z -> !z.equals(zoneInfo))
            .collect(toList()));
  }
}
