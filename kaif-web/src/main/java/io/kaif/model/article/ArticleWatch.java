package io.kaif.model.article;

import java.time.Instant;
import java.util.UUID;

import io.kaif.flake.FlakeId;

public class ArticleWatch {

  private final UUID accountId;
  private final FlakeId watchId;
  private final FlakeId articleId;
  private final Instant createTime;

  ArticleWatch(UUID accountId, FlakeId watchId, FlakeId articleId, Instant createTime) {
    this.accountId = accountId;
    this.watchId = watchId;
    this.articleId = articleId;
    this.createTime = createTime;
  }
}
