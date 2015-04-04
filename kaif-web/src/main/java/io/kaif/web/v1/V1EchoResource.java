package io.kaif.web.v1;

import static io.kaif.model.clientapp.ClientAppScope.PUBLIC;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.kaif.model.clientapp.ClientAppUserAccessToken;

@RestController
@RequestMapping("/v1/echo")
public class V1EchoResource {

  static class EchoData {
    @NotNull
    public String data;
  }

  @RequiredScope(PUBLIC)
  @RequestMapping(value = "/current-time", method = RequestMethod.GET)
  public long currentTime(ClientAppUserAccessToken accessToken) {
    return System.currentTimeMillis();
  }

  @RequiredScope(PUBLIC)
  @RequestMapping(value = "/data", method = RequestMethod.POST)
  public String postData(ClientAppUserAccessToken accessToken,
      @Valid @RequestBody EchoData echoData) {
    return echoData.data;
  }
}
