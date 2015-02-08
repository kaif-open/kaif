package io.kaif.model.vote;

import java.time.Instant;
import java.util.UUID;

import io.kaif.flake.FlakeId;

public class DebateVoter {

  public static DebateVoter create(VoteState state,
      FlakeId articleId,
      FlakeId debateId,
      UUID voterId,
      long previousCount,
      Instant now) {
    return new DebateVoter(voterId, articleId, debateId, state, previousCount, now);
  }

  private final UUID voterId;
  private final FlakeId articleId;
  private final FlakeId debateId;
  private final VoteState voteState;
  private final long previousCount;
  private final Instant updateTime;

  DebateVoter(UUID voterId,
      FlakeId articleId,
      FlakeId debateId,
      VoteState voteState,
      long previousCount,
      Instant updateTime) {
    this.voterId = voterId;
    this.articleId = articleId;
    this.debateId = debateId;
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

  public FlakeId getDebateId() {
    return debateId;
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

  @Override
  public String toString() {
    return "DebateVoter{" +
        "voterId=" + voterId +
        ", articleId=" + articleId +
        ", debateId=" + debateId +
        ", voteState=" + voteState +
        ", previousCount=" + previousCount +
        ", updateTime=" + updateTime +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DebateVoter that = (DebateVoter) o;

    if (previousCount != that.previousCount) {
      return false;
    }
    if (articleId != null ? !articleId.equals(that.articleId) : that.articleId != null) {
      return false;
    }
    if (debateId != null ? !debateId.equals(that.debateId) : that.debateId != null) {
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
    result = 31 * result + (debateId != null ? debateId.hashCode() : 0);
    result = 31 * result + (voteState != null ? voteState.hashCode() : 0);
    result = 31 * result + (int) (previousCount ^ (previousCount >>> 32));
    result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
    return result;
  }

  public DebateVoterDto toDto() {
    return new DebateVoterDto(debateId, voteState, previousCount, updateTime.toEpochMilli());
  }
}
