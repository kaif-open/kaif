package io.kaif.model.debate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.article.Article;

public class Debate {

  public static Debate create(Article article,
      FlakeId debateId,
      Debate parent,
      String content,
      Account debater,
      Instant now) {
    Optional<Debate> optParent = Optional.ofNullable(parent);
    FlakeId parentId = optParent.map(Debate::getDebateId).orElse(null);
    int nextLevel = optParent.map(Debate::getLevel).orElse(0) + 1;
    return new Debate(article.getArticleId(),
        debateId,
        parentId,
        nextLevel,
        content,
        DebateContentType.MARK_DOWN,
        debater.getAccountId(),
        debater.getUsername(),
        0,
        0,
        now,
        now);
  }

  private final FlakeId articleId;
  private final FlakeId debateId;
  private final FlakeId parentDebateId;
  private final int level;
  private final String content;
  private final DebateContentType contentType;
  private final UUID debaterId;
  private final String debaterName;
  private final long upVote;
  private final long downVote;
  private final Instant createTime;
  private final Instant lastUpdateTime;

  Debate(FlakeId articleId,
      FlakeId debateId,
      FlakeId parentDebateId,
      int level,
      String content,
      DebateContentType contentType,
      UUID debaterId,
      String debaterName,
      long upVote,
      long downVote,
      Instant createTime,
      Instant lastUpdateTime) {
    this.articleId = articleId;
    this.debateId = debateId;
    this.parentDebateId = parentDebateId;
    this.level = level;
    this.content = content;
    this.contentType = contentType;
    this.debaterId = debaterId;
    this.debaterName = debaterName;
    this.upVote = upVote;
    this.downVote = downVote;
    this.createTime = createTime;
    this.lastUpdateTime = lastUpdateTime;
  }

  public FlakeId getArticleId() {
    return articleId;
  }

  public FlakeId getDebateId() {
    return debateId;
  }

  public FlakeId getParentDebateId() {
    return parentDebateId;
  }

  public int getLevel() {
    return level;
  }

  public String getContent() {
    return content;
  }

  public DebateContentType getContentType() {
    return contentType;
  }

  public UUID getDebaterId() {
    return debaterId;
  }

  public String getDebaterName() {
    return debaterName;
  }

  public long getUpVote() {
    return upVote;
  }

  public long getDownVote() {
    return downVote;
  }

  public Instant getCreateTime() {
    return createTime;
  }

  public Instant getLastUpdateTime() {
    return lastUpdateTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Debate debate = (Debate) o;

    if (articleId != null ? !articleId.equals(debate.articleId) : debate.articleId != null) {
      return false;
    }
    if (debateId != null ? !debateId.equals(debate.debateId) : debate.debateId != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = articleId != null ? articleId.hashCode() : 0;
    result = 31 * result + (debateId != null ? debateId.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Debate{" +
        "articleId=" + articleId +
        ", debateId=" + debateId +
        ", parentDebateId=" + parentDebateId +
        ", level=" + level +
        ", content='" + content + '\'' +
        ", contentType=" + contentType +
        ", debaterId=" + debaterId +
        ", debaterName='" + debaterName + '\'' +
        ", upVote=" + upVote +
        ", downVote=" + downVote +
        ", createTime=" + createTime +
        ", lastUpdateTime=" + lastUpdateTime +
        '}';
  }
}