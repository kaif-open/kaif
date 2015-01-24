package io.kaif.model.account;

/**
 * authenticated and authorized account
 * <p>
 * do not add authorities or accountId for convenient. expose those values are security hole
 */
public class AccountAuth {

  private final String name;
  private final String accessToken;
  private final long expireTime;

  public AccountAuth(String name, String accessToken, long expireTime) {
    this.name = name;
    this.accessToken = accessToken;
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

  @Override
  public String toString() {
    return "AccountAuth{" +
        ", name='" + name + '\'' +
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
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (accessToken != null ? accessToken.hashCode() : 0);
    result = 31 * result + (int) (expireTime ^ (expireTime >>> 32));
    return result;
  }
}
