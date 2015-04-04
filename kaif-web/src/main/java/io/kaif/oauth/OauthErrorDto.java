package io.kaif.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OauthErrorDto {
  private final String error;
  private final String errorDescription;
  private final String errorUri;

  public OauthErrorDto(String error, String errorDescription, String errorUri) {
    this.error = error;
    this.errorDescription = errorDescription;
    this.errorUri = errorUri;
  }

  @JsonProperty("error")
  public String getError() {
    return error;
  }

  @JsonProperty("error_description")
  public String getErrorDescription() {
    return errorDescription;
  }

  @JsonProperty("error_uri")
  public String getErrorUri() {
    return errorUri;
  }

  @Override
  public String toString() {
    return "OauthErrorDto{" +
        "error='" + error + '\'' +
        ", errorDescription='" + errorDescription + '\'' +
        ", errorUri='" + errorUri + '\'' +
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

    OauthErrorDto that = (OauthErrorDto) o;

    if (error != null ? !error.equals(that.error) : that.error != null) {
      return false;
    }
    if (errorDescription != null
        ? !errorDescription.equals(that.errorDescription)
        : that.errorDescription != null) {
      return false;
    }
    return !(errorUri != null ? !errorUri.equals(that.errorUri) : that.errorUri != null);

  }

  @Override
  public int hashCode() {
    int result = error != null ? error.hashCode() : 0;
    result = 31 * result + (errorDescription != null ? errorDescription.hashCode() : 0);
    result = 31 * result + (errorUri != null ? errorUri.hashCode() : 0);
    return result;
  }
}
