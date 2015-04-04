package io.kaif.web.v1;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import io.kaif.model.exception.DomainException;
import io.kaif.web.support.AbstractRestExceptionHandler;

/**
 * response error json for oauth api, the json format is different from {@link
 * io.kaif.web.api.RestExceptionHandler}
 */
@ControllerAdvice(basePackageClasses = V1ExceptionHandler.class)
public class V1ExceptionHandler extends AbstractRestExceptionHandler<V1ErrorResponse> {

  @ExceptionHandler(DomainException.class)
  @ResponseBody
  public ResponseEntity<V1ErrorResponse> handleDomainException(final DomainException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.BAD_REQUEST;
    final V1ErrorResponse errorResponse = createErrorResponse(status,
        i18n(request, ex.i18nKey(), ex.i18nArgs().toArray()));
    //note that domain exception do not use detail log
    logger.warn("{} {}", guessUri(request), ex.getClass().getSimpleName());

    return new ResponseEntity<>(errorResponse, status);
  }

  @Override
  protected V1ErrorResponse createErrorResponse(HttpStatus status, String reason) {
    return new V1ErrorResponse(status.value(), reason);
  }
}
