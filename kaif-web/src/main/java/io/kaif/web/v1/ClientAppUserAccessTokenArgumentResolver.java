package io.kaif.web.v1;

import java.util.Optional;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.rometools.utils.Strings;

import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.oauth.InsufficientScopeException;
import io.kaif.oauth.InvalidTokenException;
import io.kaif.oauth.MissingBearerTokenException;
import io.kaif.service.ClientAppService;

public class ClientAppUserAccessTokenArgumentResolver implements HandlerMethodArgumentResolver {

  private final ClientAppService clientAppService;

  public ClientAppUserAccessTokenArgumentResolver(ClientAppService clientAppService) {
    this.clientAppService = clientAppService;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType() == ClientAppUserAccessToken.class;
  }

  @Override
  public ClientAppUserAccessToken resolveArgument(MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) throws Exception {
    String tokenStr = Strings.trimToEmpty(webRequest.getHeader(HttpHeaders.AUTHORIZATION));
    if (!tokenStr.startsWith("Bearer ")) {
      throw new MissingBearerTokenException();
    }
    String token = tokenStr.substring("Bearer ".length(), tokenStr.length()).trim();
    Optional<ClientAppUserAccessToken> accessToken = clientAppService.verifyAccessToken(token);
    if (!accessToken.isPresent()) {
      throw new InvalidTokenException();
    }
    RequiredScope requiredScope = parameter.getMethodAnnotation(RequiredScope.class);
    if (!accessToken.get().containsScope(requiredScope.value())) {
      throw new InsufficientScopeException(requiredScope.value());
    }
    return accessToken.get();
  }
}
