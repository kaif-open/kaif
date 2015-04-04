package io.kaif.web.v1;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.rometools.utils.Strings;

import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.service.ClientAppService;
import io.kaif.web.support.AccessDeniedException;

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
    //TODO change AccessDeniedException to oauth specific
    String tokenStr = Strings.trimToEmpty(webRequest.getHeader(HttpHeaders.AUTHORIZATION));
    if (tokenStr.startsWith("Bearer ")) {
      String token = tokenStr.substring("Bearer ".length(), tokenStr.length()).trim();
      return clientAppService.verifyAccessToken(token).orElseThrow(AccessDeniedException::new);
    }
    throw new AccessDeniedException();
  }
}
