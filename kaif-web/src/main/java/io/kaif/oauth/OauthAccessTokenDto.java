package io.kaif.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OauthAccessTokenDto {
  private final String accessToken;
  private final String scope;
  private final String tokenType;

  public OauthAccessTokenDto(String accessToken, String scope, String tokenType) {
    this.accessToken = accessToken;
    this.scope = scope;
    this.tokenType = tokenType;
  }

  @JsonProperty("access_token")
  public String getAccessToken() {
    return accessToken;
  }

  @JsonProperty("scope")
  public String getScope() {
    return scope;
  }

  @JsonProperty("token_type")
  public String getTokenType() {
    return tokenType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    OauthAccessTokenDto that = (OauthAccessTokenDto) o;

    if (accessToken != null ? !accessToken.equals(that.accessToken) : that.accessToken != null) {
      return false;
    }
    if (scope != null ? !scope.equals(that.scope) : that.scope != null) {
      return false;
    }
    return !(tokenType != null ? !tokenType.equals(that.tokenType) : that.tokenType != null);

  }

  @Override
  public int hashCode() {
    int result = accessToken != null ? accessToken.hashCode() : 0;
    result = 31 * result + (scope != null ? scope.hashCode() : 0);
    result = 31 * result + (tokenType != null ? tokenType.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "OAuthAccessTokenDto{" +
        "accessToken='" + accessToken + '\'' +
        ", scope='" + scope + '\'' +
        ", tokenType='" + tokenType + '\'' +
        '}';
  }
}
