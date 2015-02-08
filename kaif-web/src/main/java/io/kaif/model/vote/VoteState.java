package io.kaif.model.vote;

public enum VoteState {

  UP, DOWN, EMPTY;

  public int downVoteDeltaFrom(VoteState previousState) {
    if (this == DOWN && previousState != DOWN) {
      return 1;
    } else if (this != DOWN && previousState == DOWN) {
      return -1;
    }
    return 0;
  }

  public int upVoteDeltaFrom(VoteState previousState) {
    if (this == UP && previousState != UP) {
      return 1;
    } else if (this != UP && previousState == UP) {
      return -1;
    }
    return 0;
  }
}
