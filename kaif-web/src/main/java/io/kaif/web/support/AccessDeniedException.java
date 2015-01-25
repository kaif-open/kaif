package io.kaif.web.support;

public class AccessDeniedException extends RuntimeException {

  private static final long serialVersionUID = 6132085454559031915L;

  public AccessDeniedException() {
  }

  public AccessDeniedException(final String message) {
    super(message);
  }

  public AccessDeniedException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public AccessDeniedException(final String message,
      final Throwable cause,
      final boolean enableSuppression,
      final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);

  }

  public AccessDeniedException(final Throwable cause) {
    super(cause);
  }

}