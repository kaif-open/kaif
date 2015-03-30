package io.kaif.model.clientapp;

import java.util.List;
import java.util.UUID;

public class ClientAppUser {

  private final UUID clientAppUserId;
  private final UUID clientId;
  private final UUID accountId;
  private final List<ClientAppScope> scopes;
  private final String sessionKey;

  ClientAppUser(UUID clientAppUserId,
      UUID clientId,
      UUID accountId,
      List<ClientAppScope> scopes,
      String sessionKey) {
    this.clientAppUserId = clientAppUserId;
    this.clientId = clientId;
    this.accountId = accountId;
    this.scopes = scopes;
    this.sessionKey = sessionKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ClientAppUser that = (ClientAppUser) o;

    return !(clientAppUserId != null
             ? !clientAppUserId.equals(that.clientAppUserId)
             : that.clientAppUserId != null);

  }

  @Override
  public int hashCode() {
    return clientAppUserId != null ? clientAppUserId.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "ClientAppUser{" +
        "clientAppUserId=" + clientAppUserId +
        ", clientId=" + clientId +
        ", accountId=" + accountId +
        ", scopes=" + scopes +
        ", sessionKey='" + sessionKey + '\'' +
        '}';
  }
}
