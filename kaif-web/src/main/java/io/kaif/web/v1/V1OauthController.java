package io.kaif.web.v1;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Preconditions;

@Controller
@RequestMapping("/v1/oauth")
public class V1OauthController {

  @RequestMapping(value = "/authorize", method = RequestMethod.GET)
  public ModelAndView authorize(@RequestParam("client_id") String clientId,
      @RequestParam(value = "state") String state,
      @RequestParam(value = "scope") List<String> scopes,
      @RequestParam(value = "redirect_uri") String redirectUri) {
    //validate clientId, scope, redirectUri

    //TODO scope use space to separate
    return new ModelAndView("v1/authorize").addObject("clientId", clientId)
        .addObject("state", state)
        .addObject("redirectUri", redirectUri)
        .addObject("scope",
            Optional.ofNullable(scopes)
                .orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.joining(",")));
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
