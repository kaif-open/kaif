package io.kaif.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.kaif.model.exception.DomainException;
import io.kaif.web.support.AbstractRestExceptionHandler;
import io.kaif.web.support.AccessDeniedException;
import io.kaif.web.support.RestErrorResponse;

@ControllerAdvice(annotations = RestController.class)
public class RestExceptionHandler extends AbstractRestExceptionHandler {

  public static class TranslatedRestErrorResponse extends RestErrorResponse {
    private static final long serialVersionUID = 1360949999878207L;

    public TranslatedRestErrorResponse(int code, String reason) {
      super(code, reason);
    }

    @JsonProperty
    public boolean getTranslated() {
      return true;
    }

    @Override
    public String toString() {
      return "{\"code\":"
          + getCode()
          + ",\"reason\":\""
          + getReason()
          + "\""
          + ",\"translated\":true}";
    }
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseBody
  public ResponseEntity<RestErrorResponse> handleRestAccessDeniedException(final AccessDeniedException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.UNAUTHORIZED;
    final RestErrorResponse errorResponse = new RestErrorResponse(status.value(),
        i18n(request, "rest-error.RestAccessDeniedException"));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler(DomainException.class)
  @ResponseBody
  public ResponseEntity<TranslatedRestErrorResponse> handleDomainException(final DomainException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.BAD_REQUEST;
    final TranslatedRestErrorResponse errorResponse = new TranslatedRestErrorResponse(status.value(),
        i18n(request, ex.i18nKey(), ex.i18nArgs().toArray()));
    String uri = "non uri";
    if (request instanceof ServletWebRequest) {
      uri = ((ServletWebRequest) request).getRequest().getRequestURI();
    }
    //note that domain exception do not use detail log
    logger.warn("{} {}", uri, ex.getClass().getSimpleName());

    return new ResponseEntity<>(errorResponse, status);
  }
}
