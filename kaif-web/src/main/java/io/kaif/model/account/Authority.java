package io.kaif.model.account;

public enum Authority {
  //DO NOT break order, or change name, the ordinal are used to calculate many other things
  NORMAL, ZONE_ADMIN, ROOT;

  public int bit() {
    return 1 << ordinal();
  }
}
