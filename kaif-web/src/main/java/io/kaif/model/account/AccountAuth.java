package io.kaif.model.account;

import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * authenticated and authorized account
 */
public class AccountAuth {

  @JsonProperty
  private final UUID accountId;
  private final String name;
  private final String accessToken;
  private final Set<Authority> authorities;
  private final long expireTime;

  public AccountAuth(UUID accountId,
      String name,
      String accessToken,
      Set<Authority> authorities,
      long expireTime) {
    this.accountId = accountId;
    this.name = name;
    this.accessToken = accessToken;
    this.authorities = authorities;
    this.expireTime = expireTime;
  }

  public long getExpireTime() {
    return expireTime;
  }

  public String getName() {
    return name;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public Set<Authority> getAuthorities() {
    return authorities;
  }

  @Override
  public String toString() {
    return "AccountAuth{" +
        "accountId=" + accountId +
        ", name='" + name + '\'' +
        ", authorities=" + authorities +
        ", expireTime=" + expireTime +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AccountAuth that = (AccountAuth) o;

    if (expireTime != that.expireTime) {
      return false;
    }
    if (accessToken != null ? !accessToken.equals(that.accessToken) : that.accessToken != null) {
      return false;
    }
    if (accountId != null ? !accountId.equals(that.accountId) : that.accountId != null) {
      return false;
    }
    if (authorities != null ? !authorities.equals(that.authorities) : that.authorities != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = accountId != null ? accountId.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (accessToken != null ? accessToken.hashCode() : 0);
    result = 31 * result + (authorities != null ? authorities.hashCode() : 0);
    result = 31 * result + (int) (expireTime ^ (expireTime >>> 32));
    return result;
  }
}
