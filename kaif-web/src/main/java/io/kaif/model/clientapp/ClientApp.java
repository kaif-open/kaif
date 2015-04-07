package io.kaif.model.clientapp;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import io.kaif.model.account.Authorization;
import io.kaif.model.exception.CallbackUriReservedException;
import io.kaif.model.exception.ClientAppNameReservedException;

public class ClientApp {

  public static final int NAME_MAX = 15;
  public static final int NAME_MIN = 3;
  public static final int DESCRIPTION_MAX = 100;
  public static final int DESCRIPTION_MIN = 5;
  public static final String CALLBACK_URI_PATTERN = ".+://.+";
  public static final int MAX_NO_OF_APPS = 5;

  public static ClientApp create(UUID ownerAccountId,
      String name,
      String description,
      String callbackUri,
      Instant now) throws CallbackUriReservedException, ClientAppNameReservedException {
    String clientId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
    return new ClientApp(clientId,
        createNewSecret(),
        name,
        description,
        now,
        ownerAccountId,
        false,
        callbackUri);
  }

  static String createNewSecret() {
    return UUID.randomUUID().toString().replaceAll("-", "");
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
    if (callbackUri.toLowerCase().contains("kaif")) {
      throw new CallbackUriReservedException();
    }
    if (appName.equalsIgnoreCase("kaif")) {
      throw new ClientAppNameReservedException();
    }
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

  UUID getOwnerAccountId() {
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

  public ClientApp withName(String newName) {
    return new ClientApp(clientId,
        clientSecret,
        newName,
        description,
        createTime,
        ownerAccountId,
        revoked,
        callbackUri);
  }

  public ClientApp withDescription(String newDesc) {
    return new ClientApp(clientId,
        clientSecret,
        appName,
        newDesc,
        createTime,
        ownerAccountId,
        revoked,
        callbackUri);
  }

  public ClientApp withCallbackUri(String newCallback) {
    return new ClientApp(clientId,
        clientSecret,
        appName,
        description,
        createTime,
        ownerAccountId,
        revoked,
        newCallback);
  }

  public boolean validateRedirectUri(String targetRedirectUri) {
    return Optional.ofNullable(targetRedirectUri)
        .filter(target -> target.startsWith(callbackUri))
        .isPresent();
  }

  public ClientApp withResetSecret() {
    return new ClientApp(clientId,
        createNewSecret(),
        appName,
        description,
        createTime,
        ownerAccountId,
        revoked,
        callbackUri);
  }

  public boolean isOwner(Authorization authorization) {
    return authorization.belongToAccount(getOwnerAccountId());
  }
}
