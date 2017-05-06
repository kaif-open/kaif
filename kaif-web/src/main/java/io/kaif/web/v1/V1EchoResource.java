package io.kaif.web.v1;

import static io.kaif.model.clientapp.ClientAppScope.PUBLIC;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.model.exception.RequireCitizenException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;

@Api(tags = "echo", description = "Echo service for testing")
@RestController
@RequestMapping(value = "/v1/echo", produces = MediaType.APPLICATION_JSON_VALUE)
public class V1EchoResource {

  static class MessageEntry {
    @ApiModelProperty(required = true)
    @Size(min = 1, max = 1000)
    public String message;
  }

  static class ObjectEntry {
    @ApiModelProperty(required = false)
    public Map<?, ?> object;
  }

  @ApiOperation(value = "[public] Get system current time", notes = "Get system current time in milliseconds")
  @RequiredScope(PUBLIC)
  @RequestMapping(value = "/current-time", method = RequestMethod.GET)
  public long currentTime(ClientAppUserAccessToken accessToken) {
    return System.currentTimeMillis();
  }

  @ApiOperation(value = "[public] Echo input message", notes = "Echo input message to response")
  @RequiredScope(PUBLIC)
  @RequestMapping(value = "/message", method = RequestMethod.POST)
  public String message(ClientAppUserAccessToken accessToken,
      @Valid @RequestBody MessageEntry message) {
    return message.message;
  }

  @ApiOperation(value = "[public] Echo input object", notes = "Echo input object to response", hidden = true)
  @RequiredScope(PUBLIC)
  @RequestMapping(value = "/object", method = RequestMethod.POST)
  public Map<?, ?> object(ClientAppUserAccessToken accessToken, @RequestBody ObjectEntry entry) {
    return entry.object;
  }

  @ApiOperation(value = "for test", hidden = true)
  @RequiredScope(PUBLIC)
  @RequestMapping(value = "/test-failure", method = RequestMethod.POST)
  public void testFailure(ClientAppUserAccessToken accessToken,
      @Valid @RequestBody MessageEntry message) {
    throw new RequireCitizenException();
  }
}
