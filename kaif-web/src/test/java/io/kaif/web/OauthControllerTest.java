package io.kaif.web;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import io.kaif.model.clientapp.ClientApp;
import io.kaif.oauth.OauthAccessTokenDto;
import io.kaif.test.MvcIntegrationTests;
import io.kaif.web.support.AccessDeniedException;

public class OauthControllerTest extends MvcIntegrationTests {

  private ClientApp clientApp = clientApp(accountCitizen("dev1"), "app1");

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void authorize() throws Exception {
    when(clientAppService.verifyRedirectUri(clientApp.getClientId(), "foo://callback")).thenReturn(
        Optional.of(clientApp));
    mockMvc.perform(get("/oauth/authorize").param("client_id", clientApp.getClientId())
        .param("scope", "feed article")
        .param("state", "123")
        .param("response_type", "code")
        .param("redirect_uri", "foo://callback"))
        .andExpect(view().name("v1/authorize"))
        .andExpect(status().isOk())
        .andExpect(containsText("news feed"));
  }

  @Test
  public void authorize_wrong_redirect_uri() throws Exception {
    when(clientAppService.verifyRedirectUri(clientApp.getClientId(), "foo://callback")).thenReturn(
        Optional.empty());
    mockMvc.perform(get("/oauth/authorize").param("client_id", clientApp.getClientId())
        .param("scope", "feed article")
        .param("state", "123")
        .param("response_type", "code")
        .param("redirect_uri", "foo://callback"))
        .andExpect(view().name("error"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void authorize_wrong_responseType() throws Exception {
    when(clientAppService.verifyRedirectUri(clientApp.getClientId(), "foo://callback")).thenReturn(
        Optional.of(clientApp));
    mockMvc.perform(get("/oauth/authorize").param("client_id", clientApp.getClientId())
        .param("scope", "feed article")
        .param("state", "123")
        .param("redirect_uri", "foo://callback"))
        .andExpect(redirectedUrl(
            "foo://callback?error=unsupported_response_type&error_description=response_type%20must%20be%20code&error_uri=https://kaif.io&state=123"))
        .andExpect(status().isMovedPermanently());
  }

  @Test
  public void authorize_unexpected_server_error() throws Exception {
    when(clientAppService.verifyRedirectUri(clientApp.getClientId(), "foo://callback")).thenThrow(
        new RuntimeException("unexpected"));
    mockMvc.perform(get("/oauth/authorize").param("client_id", clientApp.getClientId())
        .param("scope", "feed article")
        .param("state", "123")
        .param("response_type", "code")
        .param("redirect_uri", "foo://callback"))
        .andExpect(redirectedUrl(
            "foo://callback?error=server_error&error_description=unknown%20server%20error&error_uri=https://kaif.io&state=123"))
        .andExpect(status().isMovedPermanently());
  }

  @Test
  public void authorize_missing_state() throws Exception {
    when(clientAppService.verifyRedirectUri(clientApp.getClientId(), "foo://callback")).thenReturn(
        Optional.of(clientApp));
    mockMvc.perform(get("/oauth/authorize").param("client_id", clientApp.getClientId())
        .param("scope", "feed article")
        .param("response_type", "code")
        .param("redirect_uri", "foo://callback"))
        .andExpect(redirectedUrl(
            "foo://callback?error=invalid_request&error_description=missing%20state&error_uri=https://kaif.io"))
        .andExpect(status().isMovedPermanently());
  }

  @Test
  public void authorize_wrong_scope() throws Exception {
    when(clientAppService.verifyRedirectUri(clientApp.getClientId(), "foo://callback")).thenReturn(
        Optional.of(clientApp));
    mockMvc.perform(get("/oauth/authorize").param("client_id", clientApp.getClientId())
        .param("scope", "wrong---scope")
        .param("state", "123")
        .param("response_type", "code")
        .param("redirect_uri", "foo://callback"))
        .andExpect(redirectedUrl(
            "foo://callback?error=invalid_scope&error_description=wrong%20scope&error_uri=https://kaif.io&state=123"))
        .andExpect(status().isMovedPermanently());
  }

  @Test
  public void accessToken() throws Exception {
    when(clientAppService.validateApp("client-id-foo", "client-secret-foo")).thenReturn(true);
    when(clientAppService.createOauthAccessTokenByGrantCode("code1234",
        "client-id-foo",
        "foo://callback")).thenReturn(new OauthAccessTokenDto("oauth-token",
        "public feed",
        "Bearer"));
    mockMvc.perform(post("/oauth/access-token").param("client_id", "client-id-foo")
        .param("client_secret", "client-secret-foo")
        .param("redirect_uri", "foo://callback")
        .param("grant_type", "authorization_code")
        .param("code", "code1234"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token", is("oauth-token")))
        .andExpect(jsonPath("$.token_type", is("Bearer")))
        .andExpect(jsonPath("$.scope", is("public feed")))
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(header().string("Pragma", "no-cache"));
  }

  @Test
  public void accessToken_invalid_client() throws Exception {
    when(clientAppService.validateApp("client-id-foo", "client-secret-foo")).thenReturn(false);
    mockMvc.perform(post("/oauth/access-token").param("client_id", "client-id-foo")
        .param("client_secret", "client-secret-foo")
        .param("redirect_uri", "foo://callback")
        .param("grant_type", "authorization_code")
        .param("code", "code1234"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error", is("invalid_client")))
        .andExpect(jsonPath("$.error_description", is("invalid client")));
  }

  @Test
  public void accessToken_access_denied() throws Exception {
    when(clientAppService.validateApp("client-id-foo", "client-secret-foo")).thenReturn(true);
    when(clientAppService.createOauthAccessTokenByGrantCode("code1234",
        "client-id-foo",
        "foo://callback")).thenThrow(new AccessDeniedException());
    mockMvc.perform(post("/oauth/access-token").param("client_id", "client-id-foo")
        .param("client_secret", "client-secret-foo")
        .param("redirect_uri", "foo://callback")
        .param("grant_type", "authorization_code")
        .param("code", "code1234"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("invalid_grant")))
        .andExpect(jsonPath("$.error_description", is("code is invalid")));
  }

  @Test
  public void accessToken_wrong_grant_type() throws Exception {
    mockMvc.perform(post("/oauth/access-token").param("client_id", "client-id-foo")
        .param("client_secret", "client-secret-foo")
        .param("redirect_uri", "foo://callback")
        .param("grant_type", "wrong----type")
        .param("code", "code1234"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("unsupported_grant_type")))
        .andExpect(jsonPath("$.error_description", is("grant_type must be authorization_code")));
  }

  @Test
  public void accessToken_missing_client_id() throws Exception {
    mockMvc.perform(post("/oauth/access-token").param("redirect_uri", "foo://callback")
        .param("client_secret", "client-secret-foo")
        .param("grant_type", "authorization_code")
        .param("code", "code1234"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("invalid_request")))
        .andExpect(jsonPath("$.error_description", is("missing client_id")));
  }

  @Test
  public void accessToken_missing_redirect_uri() throws Exception {
    mockMvc.perform(post("/oauth/access-token").param("client_id", "client-id-foo")
        .param("client_secret", "client-secret-foo")
        .param("grant_type", "authorization_code")
        .param("code", "code1234"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("invalid_request")))
        .andExpect(jsonPath("$.error_description", is("missing redirect_uri")));
  }

  @Test
  public void accessToken_missing_code() throws Exception {
    mockMvc.perform(post("/oauth/access-token").param("client_id", "client-id-foo")
        .param("client_secret", "client-secret-foo")
        .param("redirect_uri", "foo://callback")
        .param("grant_type", "authorization_code"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("invalid_request")))
        .andExpect(jsonPath("$.error_description", is("missing code")));
  }

  @Test
  public void directGrantCode() throws Exception {
    when(clientAppService.directGrantCode("foo", "client-id-foo", "feed article", "foo://callback"))
        .thenReturn("auth code");
    mockMvc.perform(post("/oauth/authorize").param("oauthDirectAuthorize", "foo")
        .param("client_id", "client-id-foo")
        .param("redirect_uri", "foo://callback")
        .param("scope", "feed article")
        .param("state", "123"))
        .andExpect(redirectedUrl("foo://callback?code=auth%20code&state=123"))
        .andExpect(status().isMovedPermanently())
        .andExpect(header().string("Cache-Control", "no-store"))
        .andExpect(header().string("Pragma", "no-cache"));
  }

  @Test
  public void directGrantCode_unexpected_server_error() throws Exception {
    when(clientAppService.directGrantCode("foo", "client-id-foo", "feed article", "foo://callback"))
        .thenThrow(new RuntimeException("unexpected"));
    mockMvc.perform(post("/oauth/authorize").param("oauthDirectAuthorize", "foo")
        .param("client_id", "client-id-foo")
        .param("redirect_uri", "foo://callback")
        .param("scope", "feed article")
        .param("state", "123 456"))
        .andExpect(redirectedUrl(
            "foo://callback?error=server_error&error_description=unknown%20server%20error&error_uri=https://kaif.io&state=123%20456"))
        .andExpect(status().isMovedPermanently());
  }

  @Test
  public void directGrantCode_access_denied() throws Exception {
    when(clientAppService.directGrantCode("foo", "client-id-foo", "feed article", "foo://callback"))
        .thenThrow(new AccessDeniedException());
    mockMvc.perform(post("/oauth/authorize").param("oauthDirectAuthorize", "foo")
        .param("client_id", "client-id-foo")
        .param("scope", "feed article")
        .param("state", "123 456")
        .param("redirect_uri", "foo://callback"))
        .andExpect(redirectedUrl(
            "foo://callback?error=access_denied&error_description=access%20denied&error_uri=https://kaif.io&state=123%20456"))
        .andExpect(status().isMovedPermanently());
  }

  @Test
  public void directGrantCode_grantDeny() throws Exception {
    mockMvc.perform(post("/oauth/authorize").param("oauthDirectAuthorize", "foo")
        .param("grantDeny", "true")
        .param("client_id", "client-id-foo")
        .param("scope", "feed article")
        .param("state", "123 456")
        .param("redirect_uri", "foo://callback"))
        .andExpect(redirectedUrl(
            "foo://callback?error=access_denied&error_description=access%20denied&error_uri=https://kaif.io&state=123%20456"))
        .andExpect(status().isMovedPermanently());
  }
}