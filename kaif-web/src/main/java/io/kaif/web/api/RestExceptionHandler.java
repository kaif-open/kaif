package io.kaif.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.kaif.model.exception.DomainException;
import io.kaif.web.support.AbstractRestExceptionHandler;

/**
 * the class only handle exception for official kaif web site api (dart client), it produce
 * RestErrorResponse which include code and reason.
 * <p>
 * Oauth api use different response json, see {@link io.kaif.web.v1.V1ExceptionHandler}.
 */
@ControllerAdvice(basePackageClasses = RestExceptionHandler.class)
public class RestExceptionHandler extends AbstractRestExceptionHandler<SimpleErrorResponse> {

  public static class TranslatedSimpleErrorResponse extends SimpleErrorResponse {
    private static final long serialVersionUID = 1360949999878207L;

    public TranslatedSimpleErrorResponse(int code, String reason) {
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

  @ExceptionHandler(DomainException.class)
  @ResponseBody
  public ResponseEntity<TranslatedSimpleErrorResponse> handleDomainException(final DomainException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.BAD_REQUEST;
    final TranslatedSimpleErrorResponse errorResponse = new TranslatedSimpleErrorResponse(status.value(),
        i18n(request, ex.i18nKey(), ex.i18nArgs().toArray()));
    //note that domain exception do not use detail log
    logger.warn("{} {}", guessUri(request), ex.getClass().getSimpleName());

    return new ResponseEntity<>(errorResponse, status);
  }

  @Override
  protected SimpleErrorResponse createErrorResponse(HttpStatus status, String reason) {
    return new SimpleErrorResponse(status.value(), reason);
  }
}
