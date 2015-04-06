package io.kaif.web.v1;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;

import io.kaif.flake.FlakeId;
import io.kaif.model.clientapp.ClientAppScope;
import io.kaif.model.clientapp.ClientAppUserAccessToken;

@Api(value = "feed", description = "Personal news feed")
@RestController
@RequestMapping(value = "/v1/feed", produces = MediaType.APPLICATION_JSON_VALUE)
public class V1FeedResource {

  static class MarkEntry {
    @NotNull
    public FlakeId afterAssetId;
  }

  @RequiredScope(ClientAppScope.FEED)
  @RequestMapping(value = "/news", method = RequestMethod.GET)
  public void news(ClientAppUserAccessToken token,
      @RequestParam(value = "start-asset-id", required = false) FlakeId startAssetId) {
  }

  //TODO document call once per 5 minutes
  @RequiredScope(ClientAppScope.FEED)
  @RequestMapping(value = "/news-unread-count", method = RequestMethod.GET)
  public void newsUnreadCount(ClientAppUserAccessToken token) {
  }

  @RequiredScope(ClientAppScope.FEED)
  @RequestMapping(value = "/mark-all-as-read", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void markAllAsRead(ClientAppUserAccessToken token, @Valid @RequestBody MarkEntry entry) {
  }
}
