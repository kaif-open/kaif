package io.kaif.web.v1;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import org.junit.Test;

import io.kaif.model.clientapp.ClientApp;
import io.kaif.test.MvcIntegrationTests;

public class V1OauthControllerTest extends MvcIntegrationTests {

  private ClientApp clientApp = clientApp(accountCitizen("dev1"), "app1");

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
        .andExpect(status().isOk());
  }

  @Test
  public void authorize_wrong_redirectUri() throws Exception {
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
            "foo://callback?error=unsupported_response_type&error_description=response_type%20must%20be%20code&state=123"))
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
            "foo://callback?error=invalid_request&error_description=missing%20state"))
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
            "foo://callback?error=invalid_scope&error_description=wrong%20scope&state=123"))
        .andExpect(status().isMovedPermanently());
  }
}