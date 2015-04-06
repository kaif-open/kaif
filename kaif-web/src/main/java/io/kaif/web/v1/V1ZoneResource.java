package io.kaif.web.v1;

import static io.kaif.model.clientapp.ClientAppScope.PUBLIC;
import static java.util.stream.Collectors.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;

import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.service.ZoneService;
import io.kaif.web.v1.dto.V1ZoneDto;

@Api(value = "zone", description = "Discussion zones")
@RestController
@RequestMapping(value = "/v1/zone", produces = MediaType.APPLICATION_JSON_VALUE)
public class V1ZoneResource {

  @Autowired
  private ZoneService zoneService;

  @RequiredScope(PUBLIC)
  @RequestMapping(value = "/all", method = RequestMethod.GET)
  public List<V1ZoneDto> all(ClientAppUserAccessToken token) {
    return zoneService.listCitizenZones().stream().map(ZoneInfo::toV1Dto).collect(toList());
  }

}
