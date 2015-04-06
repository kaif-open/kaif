package io.kaif.web.v1;

import static io.kaif.model.clientapp.ClientAppScope.PUBLIC;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;

import io.kaif.model.clientapp.ClientAppUserAccessToken;

@Api(value = "zone", description = "Discussion zones")
@RestController
@RequestMapping(value = "/v1/zone", produces = MediaType.APPLICATION_JSON_VALUE)
public class V1ZoneResource {

  @RequiredScope(PUBLIC)
  @RequestMapping(value = "/all", method = RequestMethod.GET)
  public void all(ClientAppUserAccessToken token) {
  }

}
