package io.kaif.model.clientapp;

import java.time.Instant;
import java.util.UUID;

public class ClientApp {
  private final UUID clientId;
  private final String clientSecret;
  private final String appName;
  private final String description;
  private final Instant createTime;
  private final UUID ownerAccountId;
  private final boolean revoked;
  private final String callbackUrl;

  ClientApp(UUID clientId,
      String clientSecret,
      String appName,
      String description,
      Instant createTime,
      UUID ownerAccountId,
      boolean revoked,
      String callbackUrl) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.appName = appName;
    this.description = description;
    this.createTime = createTime;
    this.ownerAccountId = ownerAccountId;
    this.revoked = revoked;
    this.callbackUrl = callbackUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ClientApp clientApp = (ClientApp) o;

    return !(clientId != null ? !clientId.equals(clientApp.clientId) : clientApp.clientId != null);

  }

  @Override
  public int hashCode() {
    return clientId != null ? clientId.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "ClientApp{" +
        "clientId=" + clientId +
        ", clientSecret='" + clientSecret + '\'' +
        ", appName='" + appName + '\'' +
        ", description='" + description + '\'' +
        ", createTime=" + createTime +
        ", ownerAccountId=" + ownerAccountId +
        ", revoked=" + revoked +
        '}';
  }
}
