package io.kaif.web.v1;

import static java.util.stream.Collectors.*;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;

import io.kaif.flake.FlakeId;
import io.kaif.model.clientapp.ClientAppScope;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.model.debate.Debate;
import io.kaif.model.zone.Zone;
import io.kaif.service.ArticleService;
import io.kaif.web.v1.dto.V1DebateDto;
import io.kaif.web.v1.dto.V1DebateNodeDto;

@Api(tags = "debate", description = "Debates on articles")
@RestController
@RequestMapping(value = "/v1/debate", produces = MediaType.APPLICATION_JSON_VALUE)
public class V1DebateResource {

  static class CreateDebateEntry {
    @NotNull
    @ApiModelProperty(required = true)
    public FlakeId articleId;

    @ApiModelProperty(value = "debateId reply to. null if reply to article", required = false)
    public FlakeId parentDebateId;

    @Size(min = Debate.CONTENT_MIN, max = Debate.CONTENT_MAX)
    @NotNull
    @ApiModelProperty(value = "content of debate, min 5 chars", required = true)
    public String content;
  }

  static class UpdateDebateEntry {
    @Size(min = Debate.CONTENT_MIN, max = Debate.CONTENT_MAX)
    @NotNull
    @ApiModelProperty(value = "content of debate, min 5 chars", required = true)
    public String content;
  }

  @Autowired
  private ArticleService articleService;

  @ApiOperation(value = "[public] Get a debate", notes = "Get a debate by debateId")
  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/{debateId}", method = RequestMethod.GET)
  public V1DebateDto debate(ClientAppUserAccessToken accessToken,
      @PathVariable("debateId") FlakeId debateId) {
    return articleService.loadDebateWithCache(debateId).toV1Dto();
  }

  @ApiOperation(value = "[public] Load all debates for an article",
      notes = "Load all debates (in tree structure) for an article. Note that result may be a big json.")
  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/article/{articleId}/tree", method = RequestMethod.GET)
  public V1DebateNodeDto debateTreeOfArticle(ClientAppUserAccessToken accessToken,
      @PathVariable("articleId") FlakeId articleId) {
    return articleService.listBestDebates(articleId, null).toV1Dto();
  }

  @ApiOperation(value = "[public] List latest debates of all zones",
      notes = "List latest debates of all zones, 25 debates a page. "
          + "To retrieve next page, passing last debate id of previous page in parameter start-debate-id.")
  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/latest", method = RequestMethod.GET)
  public List<V1DebateDto> latest(ClientAppUserAccessToken accessToken,
      @RequestParam(value = "start-debate-id", required = false) FlakeId startDebateId) {
    return toDtos(articleService.listLatestDebates(startDebateId));
  }

  private List<V1DebateDto> toDtos(List<Debate> debates) {
    return debates.stream().map(Debate::toV1Dto).collect(toList());
  }

  @ApiOperation(value = "[public] List latest debates for a zone",
      notes = "List latest debates for a zone, 25 debates a page. "
          + "To retrieve next page, passing last debate id of previous page in parameter start-debate-id")
  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/zone/{zone}/latest", method = RequestMethod.GET)
  public List<V1DebateDto> latestByZone(ClientAppUserAccessToken accessToken,
      @PathVariable("zone") String zone,
      @RequestParam(value = "start-debate-id", required = false) FlakeId startDebateId) {
    return toDtos(articleService.listLatestZoneDebates(Zone.valueOf(zone), startDebateId));
  }

  @ApiOperation(value = "[debate] List submitted debates of the user",
      notes = "List submitted debates of the user, 25 debates a page. "
          + "To retrieve next page, passing last debate id of previous page in parameter start-debate-id")
  @RequiredScope(ClientAppScope.DEBATE)
  @RequestMapping(value = "/user/{username}/submitted", method = RequestMethod.GET)
  public List<V1DebateDto> userSubmitted(ClientAppUserAccessToken accessToken,
      @PathVariable("username") String username,
      @RequestParam(value = "start-debate-id", required = false) FlakeId startDebateId) {
    return toDtos(articleService.listDebatesByDebater(username, startDebateId));
  }

  @ApiOperation(value = "[debate] Create a debate in the article",
      notes = "Create a debate in the article. If reply to a debate, "
          + "it should be specified in the field: parentDebateId.")
  @ResponseStatus(HttpStatus.CREATED)
  @RequiredScope(ClientAppScope.DEBATE)
  @RequestMapping(value = "", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public V1DebateDto create(ClientAppUserAccessToken accessToken,
      @Valid @RequestBody CreateDebateEntry entry) {
    return articleService.debate(entry.articleId,
        entry.parentDebateId,
        accessToken,
        entry.content.trim()).toV1Dto();
  }

  @ApiOperation(value = "[debate] Edit content of a debate",
      notes = "Edit content of a debate that user created.")
  @RequiredScope(ClientAppScope.DEBATE)
  @RequestMapping(value = "/{debateId}/content", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public String updateDebateContent(ClientAppUserAccessToken accessToken,
      @PathVariable("debateId") FlakeId debateId,
      @Valid @RequestBody UpdateDebateEntry entry) {
    return articleService.updateDebateContent(debateId, accessToken, entry.content.trim());
  }

}
