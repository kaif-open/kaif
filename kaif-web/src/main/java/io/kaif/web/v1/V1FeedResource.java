package io.kaif.web.v1;

import static java.util.stream.Collectors.*;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.annotations.ApiOperation;

import io.kaif.flake.FlakeId;
import io.kaif.model.clientapp.ClientAppScope;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.model.debate.Debate;
import io.kaif.model.feed.FeedAsset;
import io.kaif.service.ArticleService;
import io.kaif.service.FeedService;
import io.kaif.web.v1.dto.V1AssetType;
import io.kaif.web.v1.dto.V1DebateDto;
import io.kaif.web.v1.dto.V1FeedAssetDto;

@Api(value = "feed", description = "Personal news feed")
@RestController
@RequestMapping(value = "/v1/feed", produces = MediaType.APPLICATION_JSON_VALUE)
public class V1FeedResource {

  static class AcknowledgeEntry {
    @ApiModelProperty(required = true)
    @NotNull
    public FlakeId assetId;
  }

  @Autowired
  private ArticleService articleService;
  @Autowired
  private FeedService feedService;

  @ApiOperation(value = "[feed] Latest news",
      notes = "List latest FeedAssets, 25 assets a page. "
          + "To retrieve next page, passing last asset id of previous page in parameter start-asset-id.")
  @RequiredScope(ClientAppScope.FEED)
  @RequestMapping(value = "/news", method = RequestMethod.GET)
  public List<V1FeedAssetDto> news(ClientAppUserAccessToken token,
      @RequestParam(value = "start-asset-id", required = false) FlakeId startAssetId) {
    List<FeedAsset> feedAssets = feedService.listFeeds(token, startAssetId);
    List<Debate> debates = articleService.listDebatesByIdWithCache(feedAssets.stream()
        .map(FeedAsset::getAssetId)
        .collect(toList()));

    // currently only support debate from reply
    return feedAssets.stream()
        .filter(asset -> asset.getAssetType() == FeedAsset.AssetType.DEBATE_FROM_REPLY)
        .map(asset -> {
          V1DebateDto found = debates.stream()
              .filter(debate -> debate.getDebateId().equals(asset.getAssetId()))
              .map(Debate::toV1Dto)
              .findAny()
              .orElse(null);
          return new V1FeedAssetDto(asset.getAssetId().toString(),
              V1AssetType.DEBATE_FROM_REPLY,
              Date.from(asset.getCreateTime()),
              asset.isAcked(),
              found);
        })
        .collect(toList());
  }

  @ApiOperation(value = "[feed] Get unread count in news feed",
      notes = "Get unread count in news feed, max value is 11 regardless real unread count. "
          + "The method should not repeat invoke in short time, we recommend once per 5 minutes.")
  @RequiredScope(ClientAppScope.FEED)
  @RequestMapping(value = "/news-unread-count", method = RequestMethod.GET)
  public int newsUnreadCount(ClientAppUserAccessToken token) {
    return feedService.countUnread(token);
  }

  @ApiOperation(value = "[feed] Acknowledge a FeedAsset",
      notes = "A FeedAsset set to acknowledged will treat all assets before it as read. "
          + "Typically you should acknowledge the latest feedAssetId you received "
          + "when the user open news feed window in your app.")
  @RequiredScope(ClientAppScope.FEED)
  @RequestMapping(value = "/acknowledge", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void acknowledge(ClientAppUserAccessToken token,
      @Valid @RequestBody AcknowledgeEntry entry) {
    feedService.acknowledge(token, entry.assetId);
  }
}
