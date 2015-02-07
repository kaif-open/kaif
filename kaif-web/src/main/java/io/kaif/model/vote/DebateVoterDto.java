package io.kaif.model.vote;

import io.kaif.flake.FlakeId;

public class DebateVoterDto {
  private final FlakeId debateId;
  private final VoteState voteState;
  private final long previousCount;
  private final long updateTime;

  public DebateVoterDto(FlakeId debateId,
      VoteState voteState,
      long previousCount,
      long updateTime) {
    this.debateId = debateId;
    this.voteState = voteState;
    this.previousCount = previousCount;
    this.updateTime = updateTime;
  }

  @Override
  public String toString() {
    return "DebateVoterDto{" +
        "debateId=" + debateId +
        ", voteState=" + voteState +
        ", previousCount=" + previousCount +
        ", updateTime=" + updateTime +
        '}';
  }

  public FlakeId getDebateId() {
    return debateId;
  }

  public VoteState getVoteState() {
    return voteState;
  }

  public long getPreviousCount() {
    return previousCount;
  }

  public long getUpdateTime() {
    return updateTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DebateVoterDto that = (DebateVoterDto) o;

    if (previousCount != that.previousCount) {
      return false;
    }
    if (updateTime != that.updateTime) {
      return false;
    }
    if (debateId != null ? !debateId.equals(that.debateId) : that.debateId != null) {
      return false;
    }
    if (voteState != that.voteState) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = debateId != null ? debateId.hashCode() : 0;
    result = 31 * result + (voteState != null ? voteState.hashCode() : 0);
    result = 31 * result + (int) (previousCount ^ (previousCount >>> 32));
    result = 31 * result + (int) (updateTime ^ (updateTime >>> 32));
    return result;
  }
}
