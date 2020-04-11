package io.kaif.web.v1;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import io.kaif.model.exception.DomainException;
import io.kaif.oauth.InsufficientScopeException;
import io.kaif.oauth.InvalidTokenException;
import io.kaif.oauth.MissingBearerTokenException;
import io.kaif.oauth.OauthErrors;
import io.kaif.oauth.Oauths;
import io.kaif.web.support.AbstractRestExceptionHandler;

/**
 * response error json for oauth api, the json format is different from {@link
 * io.kaif.web.api.RestExceptionHandler}
 */
@ControllerAdvice(basePackageClasses = V1ExceptionHandler.class)
//make higher precedence than default spring error handling (which produce web page, not json)
@Order(-10)
public class V1ExceptionHandler extends AbstractRestExceptionHandler<V1ErrorResponse> {

  public static final String KAIF_API_REAM = "Kaif API";

  private static String realm() {
    return Oauths.OAUTH_HEADER_NAME + " " + pair(Oauths.WWWAuthHeader.REALM, KAIF_API_REAM);
  }

  private static String pair(String key, String value) {
    return String.format("%s=\"%s\"", key, value);
  }

  @ExceptionHandler(DomainException.class)
  @ResponseBody
  public ResponseEntity<V1ErrorResponse> handleDomainException(final DomainException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.BAD_REQUEST;
    String reason = i18n(request, ex.i18nKey(), ex.i18nArgs().toArray());
    final V1ErrorResponse errorResponse = new V1ErrorResponse(status.value(),
        reason,
        ex.getClass().getSimpleName(),
        true);
    //note that domain exception do not use detail log
    logger.warn("{} {}", guessUri(request), ex.getClass().getSimpleName());
    return new ResponseEntity<>(errorResponse, status);
  }

  @ExceptionHandler(MissingBearerTokenException.class)
  @ResponseBody
  public ResponseEntity<V1ErrorResponse> handleMissingBearerTokenException(final MissingBearerTokenException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.UNAUTHORIZED;
    final V1ErrorResponse errorResponse = createErrorResponse(status,
        "missing Bearer token in Authorization header");
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add(Oauths.HeaderType.WWW_AUTHENTICATE, realm());
    return new ResponseEntity<>(errorResponse, responseHeaders, status);
  }

  @ExceptionHandler(InvalidTokenException.class)
  @ResponseBody
  public ResponseEntity<V1ErrorResponse> handleInvalidTokenException(final InvalidTokenException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.UNAUTHORIZED;
    final V1ErrorResponse errorResponse = createErrorResponse(status, "invalid access token");
    HttpHeaders responseHeaders = new HttpHeaders();
    String error = pair(OauthErrors.OAUTH_ERROR, OauthErrors.ResourceResponse.INVALID_TOKEN);
    String errorDesc = pair(OauthErrors.OAUTH_ERROR_DESCRIPTION, "invalid token");
    responseHeaders.add(Oauths.HeaderType.WWW_AUTHENTICATE,
        asList(realm(), error, errorDesc).stream().collect(joining(", ")));
    return new ResponseEntity<>(errorResponse, responseHeaders, status);
  }

  @ExceptionHandler(InsufficientScopeException.class)
  @ResponseBody
  public ResponseEntity<V1ErrorResponse> handleInsufficientScopeException(final InsufficientScopeException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.FORBIDDEN;
    String title = "require scope " + ex.getRequiredScope();
    final V1ErrorResponse errorResponse = createErrorResponse(status, title);
    HttpHeaders responseHeaders = new HttpHeaders();
    String error = pair(OauthErrors.OAUTH_ERROR, OauthErrors.ResourceResponse.INSUFFICIENT_SCOPE);
    String errorDesc = pair(OauthErrors.OAUTH_ERROR_DESCRIPTION, title);
    String scope = pair("scope", ex.getRequiredScope().toString());
    responseHeaders.add(Oauths.HeaderType.WWW_AUTHENTICATE,
        asList(realm(), error, errorDesc, scope).stream().collect(joining(", ")));
    return new ResponseEntity<>(errorResponse, responseHeaders, status);
  }

  @Override
  protected V1ErrorResponse createErrorResponse(HttpStatus status, String reason) {
    return new V1ErrorResponse(status.value(), reason);
  }
}
