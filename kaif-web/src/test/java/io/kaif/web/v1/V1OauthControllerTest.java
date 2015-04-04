package io.kaif.web.v1;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import io.kaif.model.clientapp.ClientApp;
import io.kaif.test.MvcIntegrationTests;
import io.kaif.web.support.AccessDeniedException;

public class V1OauthControllerTest extends MvcIntegrationTests {

  private ClientApp clientApp = clientApp(accountCitizen("dev1"), "app1");

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void authorize() throws Exception {
    when(clientAppService.verifyRedirectUri(clientApp.getClientId(), "foo://callback")).thenReturn(
        Optional.of(clientApp));
    mockMvc.perform(get("/v1/oauth/authorize").param("client_id", clientApp.getClientId())
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
    mockMvc.perform(get("/v1/oauth/authorize").param("client_id", clientApp.getClientId())
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
    mockMvc.perform(get("/v1/oauth/authorize").param("client_id", clientApp.getClientId())
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
    mockMvc.perform(get("/v1/oauth/authorize").param("client_id", clientApp.getClientId())
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
    mockMvc.perform(get("/v1/oauth/authorize").param("client_id", clientApp.getClientId())
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
    mockMvc.perform(get("/v1/oauth/authorize").param("client_id", clientApp.getClientId())
        .param("scope", "wrong---scope")
        .param("state", "123")
        .param("response_type", "code")
        .param("redirect_uri", "foo://callback"))
        .andExpect(redirectedUrl(
            "foo://callback?error=invalid_scope&error_description=wrong%20scope&error_uri=https://kaif.io&state=123"))
        .andExpect(status().isMovedPermanently());
  }

  @Test
  public void directGrantCode() throws Exception {
    when(clientAppService.directGrantCode("foo", "client-id-foo", "feed article", "foo://callback"))
        .thenReturn("auth code");
    mockMvc.perform(post("/v1/oauth/authorize").param("oauthDirectAuthorize", "foo")
        .param("client_id", "client-id-foo")
        .param("redirect_uri", "foo://callback")
        .param("scope", "feed article")
        .param("state", "123"))
        .andExpect(redirectedUrl("foo://callback?code=auth%20code&state=123"))
        .andExpect(status().isMovedPermanently());
  }

  @Test
  public void directGrantCode_unexpected_server_error() throws Exception {
    when(clientAppService.directGrantCode("foo", "client-id-foo", "feed article", "foo://callback"))
        .thenThrow(new RuntimeException("unexpected"));
    mockMvc.perform(post("/v1/oauth/authorize").param("oauthDirectAuthorize", "foo")
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
    mockMvc.perform(post("/v1/oauth/authorize").param("oauthDirectAuthorize", "foo")
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
    mockMvc.perform(post("/v1/oauth/authorize").param("oauthDirectAuthorize", "foo")
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