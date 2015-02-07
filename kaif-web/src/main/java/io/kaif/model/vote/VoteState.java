package io.kaif.model.vote;

public enum VoteState {

  UP, DOWN, EMPTY;

  public VoteDelta downVoteDelta(VoteState previousState) {
    if (this == DOWN && previousState != DOWN) {
      return VoteDelta.INCREASED;
    } else if (this != DOWN && previousState == DOWN) {
      return VoteDelta.DECREASED;
    }
    return VoteDelta.NO_CHANGE;
  }

  public VoteDelta upVoteDelta(VoteState previousState) {
    if (this == UP && previousState != UP) {
      return VoteDelta.INCREASED;
    } else if (this != UP && previousState == UP) {
      return VoteDelta.DECREASED;
    }
    return VoteDelta.NO_CHANGE;
  }
}
