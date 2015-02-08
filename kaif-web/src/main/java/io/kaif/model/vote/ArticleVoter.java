package io.kaif.model.vote;

import java.time.Instant;
import java.util.UUID;

import io.kaif.flake.FlakeId;

public class ArticleVoter {

  public static ArticleVoter create(VoteState state,
      FlakeId articleId,
      UUID accountId,
      long previousCount,
      Instant now) {
    return new ArticleVoter(accountId, articleId, state, previousCount, now);
  }

  private final UUID voterId;
  private final FlakeId articleId;
  private final VoteState voteState;
  private final long previousCount;
  private final Instant updateTime;

  public ArticleVoter(UUID voterId,
      FlakeId articleId,
      VoteState voteState,
      long previousCount,
      Instant updateTime) {
    this.voterId = voterId;
    this.articleId = articleId;
    this.voteState = voteState;
    this.previousCount = previousCount;
    this.updateTime = updateTime;
  }

  public UUID getVoterId() {
    return voterId;
  }

  public FlakeId getArticleId() {
    return articleId;
  }

  public long getPreviousCount() {
    return previousCount;
  }

  public Instant getUpdateTime() {
    return updateTime;
  }

  public VoteState getVoteState() {
    return voteState;
  }

  public ArticleVoterDto toDto() {
    return new ArticleVoterDto(articleId, voteState, previousCount, updateTime.toEpochMilli());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ArticleVoter that = (ArticleVoter) o;

    if (previousCount != that.previousCount) {
      return false;
    }
    if (articleId != null ? !articleId.equals(that.articleId) : that.articleId != null) {
      return false;
    }
    if (updateTime != null ? !updateTime.equals(that.updateTime) : that.updateTime != null) {
      return false;
    }
    if (voteState != that.voteState) {
      return false;
    }
    if (voterId != null ? !voterId.equals(that.voterId) : that.voterId != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = voterId != null ? voterId.hashCode() : 0;
    result = 31 * result + (articleId != null ? articleId.hashCode() : 0);
    result = 31 * result + (voteState != null ? voteState.hashCode() : 0);
    result = 31 * result + (int) (previousCount ^ (previousCount >>> 32));
    result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ArticleVoter{" +
        "voterId=" + voterId +
        ", articleId=" + articleId +
        ", voteState=" + voteState +
        ", previousCount=" + previousCount +
        ", updateTime=" + updateTime +
        '}';
  }
}
