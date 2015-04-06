package io.kaif.web.v1;

import static io.kaif.model.clientapp.ClientAppScope.PUBLIC;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mangofactory.swagger.annotations.ApiIgnore;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.annotations.ApiOperation;

import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.model.exception.RequireCitizenException;

@Api(value = "echo", description = "Echo service for testing")
@RestController
@RequestMapping(value = "/v1/echo", produces = MediaType.APPLICATION_JSON_VALUE)
public class V1EchoResource {

  static class MessageEntry {
    @ApiModelProperty(required = true)
    @NotNull
    @Size(min = 1, max = 1000)
    public String message;
  }

  @ApiOperation(value = "Get system current time", notes = "Get system current time in milliseconds")
  @RequiredScope(PUBLIC)
  @RequestMapping(value = "/current-time", method = RequestMethod.GET)
  public long currentTime(ClientAppUserAccessToken accessToken) {
    return System.currentTimeMillis();
  }

  @ApiOperation(value = "Echo input message", notes = "Echo input message to response")
  @RequiredScope(PUBLIC)
  @RequestMapping(value = "/message", method = RequestMethod.POST)
  public String message(ClientAppUserAccessToken accessToken,
      @Valid @RequestBody MessageEntry message) {
    return message.message;
  }

  @ApiIgnore
  @RequiredScope(PUBLIC)
  @RequestMapping(value = "/test-failure", method = RequestMethod.POST)
  public void testFailure(ClientAppUserAccessToken accessToken,
      @Valid @RequestBody MessageEntry message) {
    throw new RequireCitizenException();
  }
}
