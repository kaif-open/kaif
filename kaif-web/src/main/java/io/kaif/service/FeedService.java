package io.kaif.service;

import java.util.List;
import java.util.UUID;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Authorization;
import io.kaif.model.feed.FeedAsset;

public interface FeedService {
  FeedAsset createReplyFeed(FlakeId debateId, UUID replyToAccountId);

  List<FeedAsset> listFeeds(Authorization authorization, FlakeId startAssetId);
}
