package io.kaif.model.vote;

public enum VoteDelta {

  INCREASED(1),
  DECREASED(-1),
  NO_CHANGE(0);

  private final long delta;

  private VoteDelta(long delta) {
    this.delta = delta;
  }

  /**
   * 0 for not changed (already voted
   * 1 for vote success
   * -1 for vote cancel
   */
  public long getChangedValue() {
    return delta;
  }

}
