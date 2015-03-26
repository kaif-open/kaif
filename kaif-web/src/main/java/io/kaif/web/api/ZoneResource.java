package io.kaif.web.api;

import static java.util.stream.Collectors.*;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.service.ZoneService;
import io.kaif.web.support.SingleWrapper;

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

  public static class CreateZone {

    @NotNull
    @Pattern(regexp = Zone.ZONE_PATTERN_STR)
    private final String zone;

    @Size(max = ZoneInfo.ALIAS_NAME_MAX)
    @NotNull
    private final String aliasName;

    public CreateZone(String zone, String aliasName) {
      this.zone = zone;
      this.aliasName = aliasName;
    }

    public String getZone() {
      return zone;
    }

    public String getAliasName() {
      return aliasName;
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

  @RequestMapping(value = "/", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void create(AccountAccessToken token,
      @Valid @RequestBody CreateZone request) {
    zoneService.createByUser(request.getZone(), request.getAliasName(), token);
  }

  @RequestMapping(value = "/zone-available")
  public SingleWrapper<Boolean> isZoneAvailable(@RequestParam("zone") String zone) {
    return SingleWrapper.of(zoneService.isZoneAvailable(zone));
  }
}
