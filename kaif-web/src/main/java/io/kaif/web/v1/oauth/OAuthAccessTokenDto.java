package io.kaif.web.v1.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuthAccessTokenDto {
  private final String accessToken;
  private final String scope;
  private final String tokenType;

  public OAuthAccessTokenDto(String accessToken, String scope, String tokenType) {
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
}
