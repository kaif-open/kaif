package io.kaif.model.clientapp;

import java.time.Instant;
import java.util.UUID;

public class ClientApp {

  public static final int NAME_MAX = 15;
  public static final int NAME_MIN = 3;
  public static final int DESCRIPTION_MAX = 100;
  public static final int DESCRIPTION_MIN = 5;

  public static ClientApp create(UUID ownerAccountId,
      String name,
      String description,
      String callbackUri,
      Instant now) {
    String clientId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
    String secret = UUID.randomUUID().toString().replaceAll("-", "");
    return new ClientApp(clientId,
        secret,
        name,
        description,
        now,
        ownerAccountId,
        false,
        callbackUri);
  }
  private final String clientId;
  private final String clientSecret;
  private final String appName;
  private final String description;
  private final Instant createTime;
  private final UUID ownerAccountId;
  private final boolean revoked;
  private final String callbackUri;

  ClientApp(String clientId,
      String clientSecret,
      String appName,
      String description,
      Instant createTime,
      UUID ownerAccountId,
      boolean revoked,
      String callbackUri) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.appName = appName;
    this.description = description;
    this.createTime = createTime;
    this.ownerAccountId = ownerAccountId;
    this.revoked = revoked;
    this.callbackUri = callbackUri;
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

  public String getClientId() {
    return clientId;
  }

  @Override
  public int hashCode() {
    return clientId != null ? clientId.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "ClientApp{" +
        "clientId=" + clientId +
        ", appName='" + appName + '\'' +
        ", description='" + description + '\'' +
        ", createTime=" + createTime +
        ", ownerAccountId=" + ownerAccountId +
        ", revoked=" + revoked +
        ", callbackUri='" + callbackUri + '\'' +
        '}';
  }

  public UUID getOwnerAccountId() {
    return ownerAccountId;
  }

  public boolean isRevoked() {
    return revoked;
  }

  public String getDescription() {
    return description;
  }

  public String getAppName() {
    return appName;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public Instant getCreateTime() {
    return createTime;
  }

  public String getCallbackUri() {
    return callbackUri;
  }
}
