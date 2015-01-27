package io.kaif.model.account;

/**
 * authenticated and authorized account
 * <p>
 * do not add authorities or accountId for convenient. expose those values are security hole
 */
public class AccountAuth {

  private final String username;
  private final String accessToken;
  private final long expireTime;
  private final long generateTime;

  public AccountAuth(String username, String accessToken, long expireTime, long generateTime) {
    this.username = username;
    this.accessToken = accessToken;
    this.expireTime = expireTime;
    this.generateTime = generateTime;
  }

  public long getExpireTime() {
    return expireTime;
  }

  public String getUsername() {
    return username;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public long getGenerateTime() {
    return generateTime;
  }

  @Override
  public String toString() {
    return "AccountAuth{" +
        "username='" + username + '\'' +
        ", expireTime=" + expireTime +
        ", generateTime=" + generateTime +
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
    if (generateTime != that.generateTime) {
      return false;
    }
    if (accessToken != null ? !accessToken.equals(that.accessToken) : that.accessToken != null) {
      return false;
    }
    if (username != null ? !username.equals(that.username) : that.username != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = username != null ? username.hashCode() : 0;
    result = 31 * result + (accessToken != null ? accessToken.hashCode() : 0);
    result = 31 * result + (int) (expireTime ^ (expireTime >>> 32));
    result = 31 * result + (int) (generateTime ^ (generateTime >>> 32));
    return result;
  }
}
