package io.kaif.model.vote;

import io.kaif.flake.FlakeId;

public class ArticleVoterDto {
  private final FlakeId articleId;
  private final VoteState voteState;
  private final long previousCount;
  private final long updateTime;

  public ArticleVoterDto(FlakeId articleId,
      VoteState voteState,
      long previousCount,
      long updateTime) {
    this.articleId = articleId;
    this.voteState = voteState;
    this.previousCount = previousCount;
    this.updateTime = updateTime;
  }

  public long getUpdateTime() {
    return updateTime;
  }

  public FlakeId getArticleId() {
    return articleId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ArticleVoterDto that = (ArticleVoterDto) o;

    if (previousCount != that.previousCount) {
      return false;
    }
    if (updateTime != that.updateTime) {
      return false;
    }
    if (articleId != null ? !articleId.equals(that.articleId) : that.articleId != null) {
      return false;
    }
    if (voteState != that.voteState) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = articleId != null ? articleId.hashCode() : 0;
    result = 31 * result + (voteState != null ? voteState.hashCode() : 0);
    result = 31 * result + (int) (previousCount ^ (previousCount >>> 32));
    result = 31 * result + (int) (updateTime ^ (updateTime >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "ArticleVoterDto{" +
        "articleId=" + articleId +
        ", voteState=" + voteState +
        ", previousCount=" + previousCount +
        ", updateTime=" + updateTime +
        '}';
  }

  public VoteState getVoteState() {
    return voteState;
  }

  public long getPreviousCount() {
    return previousCount;
  }
}
