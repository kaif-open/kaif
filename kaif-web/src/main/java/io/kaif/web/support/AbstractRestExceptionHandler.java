package io.kaif.web.support;

import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * common exception handler for restful controllers, allow convert common spring data access
 * exception and part of spring mvc exceptions(*) to json, include english error message
 * <p>
 * response body will be like:
 * <p>
 * <pre>
 * {
 *    "code': 500,
 *    "reason": "Could not read file"
 * }
 * </pre>
 * <p>
 * (*) note that not all spring mvc internal exception could be catched and translate to json, this
 * seems due to spring 4.0 bug. when @ControllerAdvice specify selectors (in our case,
 * anontations=RestController), it could not catch all spring mvc exceptions, such as
 * {@link org.springframework.web.HttpRequestMethodNotSupportedException}
 *
 * @author ingram
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public abstract class AbstractRestExceptionHandler extends ResponseEntityExceptionHandler {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private MessageSource messageSource;

  @ExceptionHandler(DataAccessException.class)
  @ResponseBody
  public ResponseEntity<RestErrorResponse> handleDataAccessException(final DataAccessException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    final RestErrorResponse errorResponse = new RestErrorResponse(status.value(),
        i18n(request, "rest-error.DataAccessException", ex.getClass().getSimpleName()));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  protected final String i18n(WebRequest request, String key, Object... args) {
    return messageSource.getMessage(key, args, "!" + key + "!", resolveLocale(request));
  }

  private Locale resolveLocale(WebRequest request) {
    if (!(request instanceof ServletWebRequest)) {
      return request.getLocale();
    }
    ServletWebRequest servletWebRequest = (ServletWebRequest) request;
    return RequestContextUtils.getLocale(servletWebRequest.getRequest());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseBody
  public ResponseEntity<RestErrorResponse> handleDataIntegrityViolationException(final DataIntegrityViolationException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.CONFLICT;
    final RestErrorResponse errorResponse = new RestErrorResponse(status.value(),
        i18n(request, "rest-error.DataIntegrityViolationException"));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler(DuplicateKeyException.class)
  @ResponseBody
  public ResponseEntity<RestErrorResponse> handleDuplicateKeyException(final DuplicateKeyException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.CONFLICT;
    final RestErrorResponse errorResponse = new RestErrorResponse(status.value(),
        i18n(request, "rest-error.DuplicateKeyException"));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(final Exception ex,
      final Object body,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    final RestErrorResponse errorResponse = new RestErrorResponse(status.value(),
        status.getReasonPhrase());
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {

    //TODO detail i18n and missing parameter name
    final String detail = ex.getBindingResult()
        .getAllErrors()
        .stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .collect(joining(", "));
    final RestErrorResponse errorResponse = new RestErrorResponse(status.value(),
        i18n(request, "rest-error.MethodArgumentNotValidException", detail));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  /**
   * Customize the response for MissingServletRequestParameterException.
   * This method delegates to
   * {@link #handleExceptionInternal(Exception, Object, org.springframework.http.HttpHeaders,
   * org.springframework.http.HttpStatus, org.springframework.web.context.request.WebRequest)}.
   *
   * @param ex
   *     the exception
   * @param headers
   *     the headers to be written to the response
   * @param status
   *     the selected response status
   * @param request
   *     the current request
   * @return a {@code ResponseEntity} instance
   */
  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    final RestErrorResponse errorResponse = new RestErrorResponse(status.value(), ex.getMessage());
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler({ OptimisticLockingFailureException.class })
  @ResponseBody
  public ResponseEntity<RestErrorResponse> handleOptimisticLockingFailureException(final OptimisticLockingFailureException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.LOCKED;
    final RestErrorResponse errorResponse = new RestErrorResponse(status.value(),
        i18n(request, "rest-error.OptimisticLockingFailureException"));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler(Exception.class)
  @ResponseBody
  public ResponseEntity<RestErrorResponse> handleOtherException(final Exception ex,
      final WebRequest request) {
    // IOException ...etc
    final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    final RestErrorResponse errorResponse = new RestErrorResponse(status.value(),
        i18n(request, "rest-error.Exception", ex.getClass().getSimpleName()));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler({ PermissionDeniedDataAccessException.class })
  @ResponseBody
  public ResponseEntity<RestErrorResponse> handlePermissionDeniedDataAccessException(final PermissionDeniedDataAccessException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.UNAUTHORIZED;
    final RestErrorResponse errorResponse = new RestErrorResponse(status.value(),
        i18n(request, "rest-error.PermissionDeniedDataAccessException"));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler({ PessimisticLockingFailureException.class })
  @ResponseBody
  public ResponseEntity<RestErrorResponse> handlePessimisticLockingFailureException(final PessimisticLockingFailureException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.LOCKED;
    final RestErrorResponse errorResponse = new RestErrorResponse(status.value(),
        i18n(request, "rest-error.PessimisticLockingFailureException"));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler({ QueryTimeoutException.class })
  @ResponseBody
  public ResponseEntity<RestErrorResponse> handleQueryTimeoutException(final QueryTimeoutException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.REQUEST_TIMEOUT;
    final RestErrorResponse errorResponse = new RestErrorResponse(status.value(),
        i18n(request, "rest-error.QueryTimeoutException"));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler(RuntimeException.class)
  @ResponseBody
  public ResponseEntity<RestErrorResponse> handleRuntimeException(final RuntimeException ex,
      final WebRequest request) {
    // Runtime Exception always hidden, we should not leak internal Exception stacktrace
    final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    final RestErrorResponse errorResponse = new RestErrorResponse(status.value(),
        i18n(request, "rest-error.RuntimeException", ex.getClass().getSimpleName()));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  protected final void logException(final Exception ex,
      final RestErrorResponse errorResponse,
      final WebRequest request) {
    final StringBuilder sb = new StringBuilder();
    sb.append(errorResponse);
    sb.append("\n");
    sb.append(request.getDescription(true));
    sb.append("\nparameters -- ");
    for (final Iterator<String> iter = request.getParameterNames(); iter.hasNext(); ) {
      final String name = iter.next();
      sb.append(name);
      sb.append(":");
      final String[] values = request.getParameterValues(name);
      if (values == null) {
        sb.append("null");
      } else if (values.length == 0) {
        sb.append("");
      } else if (values.length == 1) {
        sb.append(values[0]);
      } else {
        sb.append(Arrays.toString(values));
      }
      sb.append(" ");
    }
    logger.error(sb.toString(), ex);
  }

}
