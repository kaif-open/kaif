package io.kaif.web.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import io.kaif.web.support.AbstractRestExceptionHandler;
import io.kaif.web.support.AccessDeniedException;
import io.kaif.web.support.RestErrorResponse;

@ControllerAdvice(annotations = RestController.class)
public class RestExceptionHandler extends AbstractRestExceptionHandler {

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
}
