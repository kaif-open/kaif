package io.kaif.model.feed;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import io.kaif.flake.FlakeId;

public class FeedAsset {

  public enum AssetType {
    DEBATE_FROM_REPLY(0),
    DEBATE_FROM_WATCHED_ARTICLE(1),

    //just some idea, not implement yet:
    SITE_MAIL(10),
    NEW_FRIEND(11),;

    public static AssetType fromIndex(int index) {
      return Stream.of(values()).filter(t -> t.index == index).findAny().get();
    }

    private final int index;

    private AssetType(int index) {
      this.index = index;
    }

    public int getIndex() {
      return index;
    }

    public boolean isDebate() {
      return this == DEBATE_FROM_REPLY || this == DEBATE_FROM_WATCHED_ARTICLE;
    }
  }

  private final UUID accountId;
  private final FlakeId assetId;
  private final AssetType assetType;
  private final Instant createTime;
  private final boolean acked;

  FeedAsset(UUID accountId, FlakeId assetId, AssetType assetType, Instant createTime, boolean acked) {
    this.accountId = accountId;
    this.assetId = assetId;
    this.assetType = assetType;
    this.createTime = createTime;
    this.acked = acked;
  }
}
