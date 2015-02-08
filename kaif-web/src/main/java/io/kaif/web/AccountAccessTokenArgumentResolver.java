package io.kaif.web;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import io.kaif.model.account.AccountAccessToken;
import io.kaif.service.AccountService;
import io.kaif.web.support.AccessDeniedException;

public class AccountAccessTokenArgumentResolver implements HandlerMethodArgumentResolver {

  private final AccountService accountService;

  public AccountAccessTokenArgumentResolver(AccountService accountService) {
    this.accountService = accountService;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType() == AccountAccessToken.class;
  }

  @Override
  public AccountAccessToken resolveArgument(MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) throws Exception {
    String token = webRequest.getHeader(AccountAccessToken.HEADER_KEY);
    // we only verify in memory for all request http method
    // service layer should decide check database if mutation is critical
    return accountService.tryDecodeAccessToken(token).orElseThrow(AccessDeniedException::new);
  }
}
