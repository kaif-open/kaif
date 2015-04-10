package io.kaif.model.clientapp;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * requirement:
 * <ul>
 * <li>A client app can issue multiple access tokens for same user but ClientAppUser will be only
 * one record for specific user and client app</li>
 * </ul>
 * <p>
 * the table are design to be deletion
 * <p>
 * following cases will cause delete on ClientAppUser
 * <p>
 * <ul>
 * <li>User revoke the app</li>
 * <li>App owner delete app (revoke) (cause delete all users)</li>
 * </ul>
 */
public class ClientAppUser {

  public static ClientAppUser create(String clientId,
      String clientSecret,
      UUID accountId,
      Set<ClientAppScope> scopes,
      Instant now) {
    return new ClientAppUser(UUID.randomUUID(), clientId, clientSecret, accountId, scopes, now);
  }

  private final UUID clientAppUserId;
  private final Instant lastUpdateTime;
  //clientId+accountId is unique
  private final String clientId;
  private final UUID accountId;
  //lastGrantedScopes only stores last issued access token, this is for user reference only.
  //we don't validate access token against this value in database.
  private final Set<ClientAppScope> lastGrantedScopes;
  //this value is not denormalized, it is query by join with ClientApp, so it always up to date
  private final String currentClientSecret;

  ClientAppUser(UUID clientAppUserId,
      String clientId,
      String currentClientSecret,
      UUID accountId,
      Set<ClientAppScope> lastGrantedScopes,
      Instant lastUpdateTime) {
    this.clientAppUserId = clientAppUserId;
    this.clientId = clientId;
    this.accountId = accountId;
    this.lastGrantedScopes = lastGrantedScopes;
    this.currentClientSecret = currentClientSecret;
    this.lastUpdateTime = lastUpdateTime;
  }

  public String getCurrentClientSecret() {
    return currentClientSecret;
  }

  public UUID getClientAppUserId() {
    return clientAppUserId;
  }

  public Instant getLastUpdateTime() {
    return lastUpdateTime;
  }

  public String getClientId() {
    return clientId;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public Set<ClientAppScope> getLastGrantedScopes() {
    return lastGrantedScopes;
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

  public ClientAppUser withScopes(Set<ClientAppScope> scopes) {
    return new ClientAppUser(clientAppUserId,
        clientId,
        currentClientSecret,
        accountId,
        scopes,
        lastUpdateTime);
  }

  public ClientAppUser withClientSecret(String clientSecret) {
    return new ClientAppUser(clientAppUserId,
        clientId,
        clientSecret,
        accountId,
        lastGrantedScopes,
        lastUpdateTime);
  }

  public ClientAppUser withLastUpdateTime(Instant updateTime) {
    return new ClientAppUser(clientAppUserId,
        clientId,
        currentClientSecret,
        accountId,
        lastGrantedScopes,
        updateTime);
  }
}
