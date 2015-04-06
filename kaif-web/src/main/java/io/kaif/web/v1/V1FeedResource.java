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

  //TODO document call once per 5 minutes, max value is 11
  @RequiredScope(ClientAppScope.FEED)
  @RequestMapping(value = "/news-unread-count", method = RequestMethod.GET)
  public int newsUnreadCount(ClientAppUserAccessToken token) {
    return feedService.countUnread(token);
  }

  @RequiredScope(ClientAppScope.FEED)
  @RequestMapping(value = "/acknowledge", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void acknowledge(ClientAppUserAccessToken token,
      @Valid @RequestBody AcknowledgeEntry entry) {
    feedService.acknowledge(token, entry.assetId);
  }
}
