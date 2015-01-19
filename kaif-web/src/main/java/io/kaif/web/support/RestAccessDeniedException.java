package io.kaif.web.support;

public class RestAccessDeniedException extends RuntimeException {

  private static final long serialVersionUID = 6132085454559031915L;

  public RestAccessDeniedException() {}

  public RestAccessDeniedException(final String message) {
    super(message);
  }

  public RestAccessDeniedException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public RestAccessDeniedException(final String message,
      final Throwable cause,
      final boolean enableSuppression,
      final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);

  }

  public RestAccessDeniedException(final Throwable cause) {
    super(cause);
  }

}