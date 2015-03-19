package io.kaif.web.api;

import static java.util.stream.Collectors.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.kaif.service.ZoneService;

@RestController
@RequestMapping("/api/zone")
public class ZoneResource {

  public static class ZoneDto {
    private final String name;
    private final String title;

    public ZoneDto(String name, String title) {
      this.name = name;
      this.title = title;
    }

    public String getName() {
      return name;
    }

    public String getTitle() {
      return title;
    }
  }

  @Autowired
  private ZoneService zoneService;

  @RequestMapping(value = "/all", method = RequestMethod.GET)
  public List<ZoneDto> listCitizenZones() {
    return zoneService.listCitizenZones()
        .stream()
        .map(z -> new ZoneDto(z.getZone().value(), z.getAliasName()))
        .collect(toList());
  }

}
