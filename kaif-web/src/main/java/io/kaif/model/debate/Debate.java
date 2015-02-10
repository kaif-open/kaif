package io.kaif.model.debate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import io.kaif.flake.FlakeId;
import io.kaif.kmark.KmarkProcessor;
import io.kaif.model.account.Account;
import io.kaif.model.article.Article;

public class Debate {

  public static final FlakeId NO_PARENT = FlakeId.MIN;
  private static final int MAX_LEVEL = 10;
  public static final int CONTENT_MIN = 10;
  public static final int CONTENT_MAX = 4096;

  public static Debate create(Article article,
      FlakeId debateId,
      @Nullable Debate parent,
      String content,
      Account debater,
      Instant now) {
    Optional<Debate> optParent = Optional.ofNullable(parent);
    FlakeId parentId = optParent.map(Debate::getDebateId).orElse(NO_PARENT);
    int parentLevel = optParent.map(Debate::getLevel).orElse(0);
    return new Debate(article.getArticleId(),
        debateId,
        parentId,
        parentLevel + 1,
        content,
        new KmarkProcessor().process(content, debateId.toString()),
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
  private final String renderContent;
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
      String renderContent,
      DebateContentType contentType,
      UUID debaterId,
      String debaterName,
      long upVote,
      long downVote,
      Instant createTime,
      Instant lastUpdateTime) {
    Preconditions.checkArgument(level <= MAX_LEVEL);
    Preconditions.checkArgument(!NO_PARENT.equals(debateId));
    this.articleId = articleId;
    this.debateId = debateId;
    this.parentDebateId = parentDebateId;
    this.level = level;
    this.content = content;
    this.renderContent = renderContent;
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

  FlakeId getParentDebateId() {
    return parentDebateId;
  }

  public boolean hasParent() {
    return !parentDebateId.equals(NO_PARENT);
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

  public String getRenderContent() {
    return renderContent;
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
        ", renderContent='" + renderContent + '\'' +
        ", contentType=" + contentType +
        ", debaterId=" + debaterId +
        ", debaterName='" + debaterName + '\'' +
        ", upVote=" + upVote +
        ", downVote=" + downVote +
        ", createTime=" + createTime +
        ", lastUpdateTime=" + lastUpdateTime +
        '}';
  }

  public long getTotalVote() {
    return upVote - downVote;
  }

  public boolean isMaxLevel() {
    return level >= MAX_LEVEL;
  }

  public boolean isParent(Debate child) {
    return parentDebateId.equals(child.debateId);
  }
}