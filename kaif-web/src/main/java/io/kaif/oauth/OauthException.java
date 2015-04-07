package io.kaif.oauth;

public abstract class OauthException extends RuntimeException {

  public OauthException() {
  }

  public OauthException(String message) {
    super(message);
  }

  public OauthException(String message, Throwable cause) {
    super(message, cause);
  }

  public OauthException(Throwable cause) {
    super(cause);
  }

  public OauthException(String message,
      Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
