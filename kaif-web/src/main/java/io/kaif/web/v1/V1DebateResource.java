package io.kaif.web.v1;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;

import io.kaif.flake.FlakeId;
import io.kaif.model.clientapp.ClientAppScope;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.model.debate.Debate;

@Api(value = "debate", description = "Debates on articles")
@RestController
@RequestMapping(value = "/v1/debate", produces = MediaType.APPLICATION_JSON_VALUE)
public class V1DebateResource {

  static class CreateDebateEntry {
    @NotNull
    public FlakeId articleId;

    public FlakeId parentDebateId;

    @Size(min = Debate.CONTENT_MIN, max = Debate.CONTENT_MAX)
    @NotNull
    public String content;
  }

  static class UpdateDebateEntry {
    @Size(min = Debate.CONTENT_MIN, max = Debate.CONTENT_MAX)
    @NotNull
    public String content;
  }

  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/{debateId}", method = RequestMethod.GET)
  public void debate(ClientAppUserAccessToken accessToken,
      @PathVariable("debateId") FlakeId debateId) {
  }

  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/latest", method = RequestMethod.GET)
  public void latest(ClientAppUserAccessToken accessToken,
      @RequestParam(value = "start-debate-id", required = false) FlakeId startDebateId) {
  }

  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/zone/{zone}/latest", method = RequestMethod.GET)
  public void latestByZone(ClientAppUserAccessToken accessToken,
      @PathVariable("zone") String zone,
      @RequestParam(value = "start-debate-id", required = false) FlakeId startDebateId) {
  }

  @RequiredScope(ClientAppScope.DEBATE)
  @RequestMapping(value = "/submitted", method = RequestMethod.GET)
  public void submitted(ClientAppUserAccessToken accessToken,
      @RequestParam(value = "start-debate-id", required = false) FlakeId startDebateId) {
  }

  @RequiredScope(ClientAppScope.DEBATE)
  @RequestMapping(value = "", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void create(ClientAppUserAccessToken accessToken,
      @Valid @RequestBody CreateDebateEntry entry) {

  }

  @RequiredScope(ClientAppScope.DEBATE)
  @RequestMapping(value = "/{debateId}/content", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void updateDebateContent(ClientAppUserAccessToken accessToken,
      @PathVariable("debateId") FlakeId debateId,
      @Valid @RequestBody UpdateDebateEntry entry) {

  }

}
