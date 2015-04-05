package io.kaif.web;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;

import io.kaif.model.clientapp.ClientApp;
import io.kaif.model.clientapp.ClientAppScope;
import io.kaif.oauth.GrantType;
import io.kaif.oauth.OauthErrorDto;
import io.kaif.oauth.OauthErrors;
import io.kaif.service.ClientAppService;
import io.kaif.web.support.AccessDeniedException;

@Controller
@RequestMapping("/oauth")
public class OauthController {

  private static final Logger logger = LoggerFactory.getLogger(OauthController.class);
  //TODO use right oauth error uri
  private static final String DEFAULT_ERROR_URI = "https://kaif.io";

  @Autowired
  private ClientAppService clientAppService;

  @RequestMapping(value = "/authorize", method = RequestMethod.GET)
  public Object authorize(HttpServletResponse response,
      @RequestParam(value = "client_id", required = false) String clientId,
      @RequestParam(value = "state", required = false) String state,
      @RequestParam(value = "scope", required = false) String scope,
      @RequestParam(value = "response_type", required = false) String responseType,
      @RequestParam(value = "redirect_uri", required = false) String redirectUri) {
    try {
      Optional<ClientApp> clientApp = clientAppService.verifyRedirectUri(clientId, redirectUri);
      if (!clientApp.isPresent()) {
        logger.warn("invalid redirect uri, may be attack: {}, {}", clientId, redirectUri);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return new ModelAndView("error");
      }
      if (!"code".equals(responseType)) {
        return redirectViewWithError(redirectUri,
            OauthErrors.CodeResponse.UNSUPPORTED_RESPONSE_TYPE,
            "response_type must be code",
            state);
      }
      if (Strings.isNullOrEmpty(state)) {
        return redirectViewWithError(redirectUri,
            OauthErrors.CodeResponse.INVALID_REQUEST,
            "missing state",
            state);
      }
      Set<ClientAppScope> clientAppScopes = ClientAppScope.tryParse(scope);
      if (clientAppScopes.isEmpty()) {
        return redirectViewWithError(redirectUri,
            OauthErrors.CodeResponse.INVALID_SCOPE,
            "wrong scope",
            state);
      }
      //TODO handle error=temporary_unavailable
      return new ModelAndView("v1/authorize").addObject("clientApp", clientApp.get())
          .addObject("clientAppScopes", clientAppScopes);
    } catch (RuntimeException e) {
      logger.warn("unexpected error while GET /authorize", e);
      return redirectViewWithError(redirectUri,
          OauthErrors.CodeResponse.SERVER_ERROR,
          "unknown server error",
          state);
    }
  }

  @RequestMapping(value = "/authorize", method = RequestMethod.POST)
  public Object grantCode(HttpServletResponse response,
      @RequestParam(value = "grantDeny", required = false) Boolean grantDeny,
      @RequestParam(value = "oauthDirectAuthorize") String oauthDirectAuthorize,
      @RequestParam(value = "client_id") String clientId,
      @RequestParam(value = "state") String state,
      @RequestParam(value = "scope") String scope,
      @RequestParam(value = "redirect_uri") String redirectUri) {
    setResponseNoCache(response);
    try {
      if (Optional.ofNullable(grantDeny).filter(deny -> deny).isPresent()) {
        throw new AccessDeniedException("user cancel");
      }
      final String code = clientAppService.directGrantCode(oauthDirectAuthorize,
          clientId,
          scope,
          redirectUri);
      return redirectViewWithQuery(redirectUri, state, "code=" + code);
    } catch (AccessDeniedException e) {
      return redirectViewWithError(redirectUri,
          OauthErrors.CodeResponse.ACCESS_DENIED,
          "access denied",
          state);
    } catch (RuntimeException e) {
      logger.warn("unexpected error while PUT /authorize", e);
      return redirectViewWithError(redirectUri,
          OauthErrors.CodeResponse.SERVER_ERROR,
          "unknown server error",
          state);
    }
  }

  private RedirectView redirectViewWithError(String redirectUri,
      String error,
      String errorDescription,
      String state) {
    String query = String.format("%s=%s&%s=%s&%s=%s",
        OauthErrors.OAUTH_ERROR,
        error,
        OauthErrors.OAUTH_ERROR_DESCRIPTION,
        errorDescription,
        OauthErrors.OAUTH_ERROR_URI,
        DEFAULT_ERROR_URI);
    return redirectViewWithQuery(redirectUri, state, query);
  }

  private RedirectView redirectViewWithQuery(String redirectUri, String state, String query) {
    try {
      if (!Strings.isNullOrEmpty(state)) {
        query += "&state=" + state;
      }
      String encoded = UriUtils.encodeQuery(query, Charsets.UTF_8.name());
      String locationUri = redirectUri;
      if (redirectUri.contains("?")) {
        locationUri += "&" + encoded;
      } else {
        locationUri += "?" + encoded;
      }
      RedirectView redirectView = new RedirectView(locationUri);
      redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
      redirectView.setExposeModelAttributes(false);
      redirectView.setPropagateQueryParams(false);
      return redirectView;
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * spec is POST with application/x-www-form-urlencoded and return JSON
   * <p>
   * we allow other `accept` for friendly usage, but may be this is insecure ?
   */
  @RequestMapping(value = "/access-token", method = RequestMethod.POST)
  @ResponseBody
  public Object accessToken(HttpServletResponse response,
      @RequestParam(value = "client_id", required = false) String clientId,
      @RequestParam(value = "client_secret", required = false) String clientSecret,
      @RequestParam(value = "grant_type", required = false) String grantType,
      @RequestParam(value = "code", required = false) String code,
      @RequestParam(value = "redirect_uri", required = false) String redirectUri) {
    setResponseNoCache(response);
    if (!GrantType.AUTHORIZATION_CODE.toString().equals(grantType)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new OauthErrorDto(OauthErrors.TokenResponse.UNSUPPORTED_GRANT_TYPE,
          "grant_type must be authorization_code",
          DEFAULT_ERROR_URI);
    }
    if (Strings.isNullOrEmpty(code)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new OauthErrorDto(OauthErrors.TokenResponse.INVALID_REQUEST,
          "missing code",
          DEFAULT_ERROR_URI);
    }
    if (Strings.isNullOrEmpty(clientId)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new OauthErrorDto(OauthErrors.TokenResponse.INVALID_REQUEST,
          "missing client_id",
          DEFAULT_ERROR_URI);
    }
    if (Strings.isNullOrEmpty(redirectUri)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new OauthErrorDto(OauthErrors.TokenResponse.INVALID_REQUEST,
          "missing redirect_uri",
          DEFAULT_ERROR_URI);
    }
    if (!clientAppService.validateApp(clientId, clientSecret)) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return new OauthErrorDto(OauthErrors.TokenResponse.INVALID_CLIENT,
          "invalid client",
          DEFAULT_ERROR_URI);
    }
    try {
      return clientAppService.createOauthAccessTokenByGrantCode(code, clientId, redirectUri);
    } catch (AccessDeniedException e) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new OauthErrorDto(OauthErrors.TokenResponse.INVALID_GRANT,
          "code is invalid",
          DEFAULT_ERROR_URI);
    }
  }

  void setResponseNoCache(HttpServletResponse response) {
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
    response.setHeader(HttpHeaders.PRAGMA, "no-cache");
  }

}
