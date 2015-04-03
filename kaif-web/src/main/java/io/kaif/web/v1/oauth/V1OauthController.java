package io.kaif.web.v1.oauth;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.kaif.model.clientapp.ClientApp;
import io.kaif.model.clientapp.ClientAppScope;
import io.kaif.service.ClientAppService;
import io.kaif.web.support.AccessDeniedException;

@Controller
@RequestMapping("/v1/oauth")
public class V1OauthController {

  @Autowired
  private ClientAppService clientAppService;

  @RequestMapping(value = "/authorize", method = RequestMethod.GET)
  public Object authorize(HttpServletResponse response,
      @RequestParam(value = "client_id", required = false) String clientId,
      @RequestParam(value = "state", required = false) String state,
      @RequestParam(value = "scope", required = false) String scope,
      @RequestParam(value = "response_type", required = false) String responseType,
      @RequestParam(value = "redirect_uri", required = false) String redirectUri) {
    Optional<ClientApp> clientApp = clientAppService.verifyRedirectUri(clientId, redirectUri);
    if (!clientApp.isPresent()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return new ModelAndView("error");
    }
    if (!"code".equals(responseType)) {
      return redirectViewWithError(redirectUri,
          OAuthError.CodeResponse.UNSUPPORTED_RESPONSE_TYPE,
          "response_type must be code",
          state);
    }
    if (Strings.isNullOrEmpty(state)) {
      return redirectViewWithError(redirectUri,
          OAuthError.CodeResponse.INVALID_REQUEST,
          "missing state",
          state);
    }
    Set<ClientAppScope> clientAppScopes = ClientAppScope.tryParse(scope);
    if (clientAppScopes.isEmpty()) {
      return redirectViewWithError(redirectUri,
          OAuthError.CodeResponse.INVALID_SCOPE,
          "wrong scope",
          state);
    }
    //TODO handle error=server_error and error=temporary_unavailable
    return new ModelAndView("v1/authorize").addObject("clientApp", clientApp.get())
        .addObject("clientAppScopes", clientAppScopes);
  }

  @RequestMapping(value = "/authorize", method = RequestMethod.POST)
  public Object grantCode(@RequestParam(value = "grantDeny", required = false) Boolean grantDeny,
      @RequestParam(value = "oauthDirectAuthorize") String oauthDirectAuthorize,
      @RequestParam(value = "client_id") String clientId,
      @RequestParam(value = "state") String state,
      @RequestParam(value = "scope") String scope,
      @RequestParam(value = "redirect_uri") String redirectUri) {
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
          OAuthError.CodeResponse.ACCESS_DENIED,
          "access denied",
          state);
    }
  }

  private RedirectView redirectViewWithError(String redirectUri,
      String error,
      String errorDescription,
      String state) {
    String query = String.format("%s=%s&%s=%s",
        OAuthError.OAUTH_ERROR,
        error,
        OAuthError.OAUTH_ERROR_DESCRIPTION,
        errorDescription);
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
   */
  @RequestMapping(value = "/access-token", method = RequestMethod.POST)
  @ResponseBody
  public OAuthAccessTokenDto accessToken(@RequestParam("client_id") String clientId,
      @RequestParam("grant_type") String grantType,
      @RequestParam("code") String code,
      @RequestParam("redirect_uri") String redirectUri) {

    Preconditions.checkArgument(grantType.equals("authorization_code"));

    //TODO check clientId and code on ClientAppUser
    //TODO check redirectUri match
    return new OAuthAccessTokenDto(UUID.randomUUID().toString(), "user,feed", "bearer");
  }

  @RequestMapping(value = "/xxxauthorize", method = RequestMethod.GET)
  public void endUserAuthorizationEndpoint(HttpServletRequest request, HttpServletResponse response)
      throws OAuthSystemException, IOException {

    String appDefaultRedirectUri = "http://google.com";
    try {
      OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(request);
      //dynamically recognize an OAuth profile based on request characteristic (params,
      // method, content type etc.), perform validation
      validateRedirectionURI(oauthRequest);
      OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

      //build OAuth response
      OAuthResponse resp = OAuthASResponse.authorizationResponse(request,
          HttpServletResponse.SC_FOUND)
          .setCode(oauthIssuerImpl.authorizationCode())
          .location(oauthRequest.getRedirectURI())
          .buildQueryMessage();

      response.sendRedirect(resp.getLocationUri());

      //if something goes wrong
    } catch (OAuthProblemException ex) {
      final OAuthResponse resp = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
          .error(ex)
          .location(appDefaultRedirectUri)
          .buildQueryMessage();

      response.sendRedirect(resp.getLocationUri());
    }
  }

  private void validateRedirectionURI(OAuthAuthzRequest oauthRequest) {
    //TODO
    // see github rule
    // https://developer.github.com/v3/oauth/#redirect-urls
  }

  @RequestMapping(value = "/xxxaccess-token", method = RequestMethod.POST)
  public void tokenEndpoint(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, OAuthSystemException {

    OAuthTokenRequest oauthRequest = null;

    OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

    try {
      oauthRequest = new OAuthTokenRequest(request);

      String authzCode = oauthRequest.getCode();

      // some code
      String accessToken = oauthIssuerImpl.accessToken();
      String refreshToken = oauthIssuerImpl.refreshToken();

      // some code
      OAuthResponse r = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK)
          .setAccessToken(accessToken)
          .setExpiresIn("3600")
          .setRefreshToken(refreshToken)
          .buildJSONMessage();

      response.setStatus(r.getResponseStatus());
      PrintWriter pw = response.getWriter();
      pw.print(r.getBody());
      pw.flush();
      pw.close();
      //if something goes wrong
    } catch (OAuthProblemException ex) {

      OAuthResponse r = OAuthResponse.errorResponse(401).error(ex).buildJSONMessage();

      response.setStatus(r.getResponseStatus());

      PrintWriter pw = response.getWriter();
      pw.print(r.getBody());
      pw.flush();
      pw.close();

      response.sendError(401);
    }

  }

}
