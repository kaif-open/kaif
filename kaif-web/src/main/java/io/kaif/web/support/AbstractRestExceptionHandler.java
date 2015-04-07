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
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
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

import io.kaif.config.SpringProfile;

/**
 * common exception handler for restful controllers, allow convert common spring data access
 * exception and part of spring mvc exceptions(*) to json, include english error message
 * <p>
 * subclass should implements {@link #createErrorResponse(HttpStatus, String)} to create error json
 * <p>
 * (*) note that not all spring mvc internal exception could be catched and translate to json, this
 * seems due to spring 4.0 bug. when @ControllerAdvice specify selectors (in our case,
 * anontations=RestController), it could not catch all spring mvc exceptions, such as
 * {@link org.springframework.web.HttpRequestMethodNotSupportedException}
 *
 * @author ingram
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public abstract class AbstractRestExceptionHandler<E extends ErrorResponse>
    extends ResponseEntityExceptionHandler {

  protected final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private MessageSource messageSource;

  @Autowired
  private Environment environment;

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseBody
  public ResponseEntity<E> handleAccessDeniedException(final AccessDeniedException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.UNAUTHORIZED;
    final E errorResponse = createErrorResponse(status,
        i18n(request, "rest-error.RestAccessDeniedException"));
    if (environment.acceptsProfiles(SpringProfile.DEV)) {
      //only dev server log detail access denied
      logException(ex, errorResponse, request);
    } else {
      logger.warn("{} {}", guessUri(request), ex.getClass().getSimpleName());
    }
    return new ResponseEntity<>(errorResponse, status);
  }

  protected final String guessUri(WebRequest request) {
    String uri = "non uri";
    if (request instanceof ServletWebRequest) {
      uri = ((ServletWebRequest) request).getRequest().getRequestURI();
    }
    return uri;
  }

  @ExceptionHandler(DataAccessException.class)
  @ResponseBody
  public ResponseEntity<E> handleDataAccessException(final DataAccessException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    final E errorResponse = createErrorResponse(status,
        i18n(request, "rest-error.DataAccessException", ex.getClass().getSimpleName()));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  protected abstract E createErrorResponse(HttpStatus status, String reason);

  @ExceptionHandler(EmptyResultDataAccessException.class)
  @ResponseBody
  public ResponseEntity<E> handleEmptyResultDataAccessException(final EmptyResultDataAccessException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.NOT_FOUND;
    final E errorResponse = createErrorResponse(status,
        i18n(request, "rest-error.EmptyResultDataAccessException", ex.getClass().getSimpleName()));
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
  public ResponseEntity<E> handleDataIntegrityViolationException(final DataIntegrityViolationException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.CONFLICT;
    final E errorResponse = createErrorResponse(status,
        i18n(request, "rest-error.DataIntegrityViolationException"));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler(DuplicateKeyException.class)
  @ResponseBody
  public ResponseEntity<E> handleDuplicateKeyException(final DuplicateKeyException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.CONFLICT;
    final E errorResponse = createErrorResponse(status,
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
    final E errorResponse = createErrorResponse(status, status.getReasonPhrase());
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
    final E errorResponse = createErrorResponse(status,
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
    final E errorResponse = createErrorResponse(status, ex.getMessage());
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler({ OptimisticLockingFailureException.class })
  @ResponseBody
  public ResponseEntity<E> handleOptimisticLockingFailureException(final OptimisticLockingFailureException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.LOCKED;
    final E errorResponse = createErrorResponse(status,
        i18n(request, "rest-error.OptimisticLockingFailureException"));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler(Exception.class)
  @ResponseBody
  public ResponseEntity<E> handleOtherException(final Exception ex, final WebRequest request) {
    // IOException ...etc
    final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    final E errorResponse = createErrorResponse(status,
        i18n(request, "rest-error.Exception", ex.getClass().getSimpleName()));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler({ PermissionDeniedDataAccessException.class })
  @ResponseBody
  public ResponseEntity<E> handlePermissionDeniedDataAccessException(final PermissionDeniedDataAccessException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.UNAUTHORIZED;
    final E errorResponse = createErrorResponse(status,
        i18n(request, "rest-error.PermissionDeniedDataAccessException"));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler({ PessimisticLockingFailureException.class })
  @ResponseBody
  public ResponseEntity<E> handlePessimisticLockingFailureException(final PessimisticLockingFailureException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.LOCKED;
    final E errorResponse = createErrorResponse(status,
        i18n(request, "rest-error.PessimisticLockingFailureException"));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler({ QueryTimeoutException.class })
  @ResponseBody
  public ResponseEntity<E> handleQueryTimeoutException(final QueryTimeoutException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.REQUEST_TIMEOUT;
    final E errorResponse = createErrorResponse(status,
        i18n(request, "rest-error.QueryTimeoutException"));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler(RuntimeException.class)
  @ResponseBody
  public ResponseEntity<E> handleRuntimeException(final RuntimeException ex,
      final WebRequest request) {
    // Runtime Exception always hidden, we should not leak internal Exception stacktrace
    final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    final E errorResponse = createErrorResponse(status,
        i18n(request, "rest-error.RuntimeException", ex.getClass().getSimpleName()));
    logException(ex, errorResponse, request);
    return new ResponseEntity<>(errorResponse, status);
  }

  protected final void logException(final Exception ex,
      final E errorResponse,
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
