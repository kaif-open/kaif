package io.kaif.web.v1;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.EnumSet;
import java.util.Optional;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import io.kaif.model.account.Account;
import io.kaif.model.clientapp.ClientAppScope;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.test.MvcIntegrationTests;

public class ClientAppUserAccessTokenArgumentResolverTest extends MvcIntegrationTests {
  @Test
  public void missingBearerToken() throws Exception {
    mockMvc.perform(get("/v1/echo/current-time")//
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(header().string("WWW-Authenticate", "Bearer realm=\"Kaif API\""));
    mockMvc.perform(get("/v1/echo/current-time")//
        .header(HttpHeaders.AUTHORIZATION, "Bearer  ").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(header().string("WWW-Authenticate", "Bearer realm=\"Kaif API\""));
  }

  @Test
  public void invalidToken() throws Exception {
    when(clientAppService.verifyAccessToken("bad-token")).thenReturn(Optional.empty());
    mockMvc.perform(get("/v1/echo/current-time")//
        .header(HttpHeaders.AUTHORIZATION, "Bearer bad-token")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized())
        .andExpect(header().string("WWW-Authenticate",
            q("Bearer realm='Kaif API', error='invalid_token', error_description='invalid token'")));
  }

  @Test
  public void insufficientScope() throws Exception {
    Account account = accountCitizen("user1");
    ClientAppUserAccessToken token = new ClientAppUserAccessToken(account.getAccountId(),
        account.getAuthorities(),
        EnumSet.of(ClientAppScope.ARTICLE),
        account.getUsername() + "-client-id",
        account.getUsername() + "-client-secret");
    when(clientAppService.verifyAccessToken("a-token")).thenReturn(Optional.of(token));

    mockMvc.perform(get("/v1/echo/current-time")//
        .header(HttpHeaders.AUTHORIZATION, "Bearer a-token ")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden())
        .andExpect(header().string("WWW-Authenticate",
            q("Bearer realm='Kaif API', error='insufficient_scope', error_description='require scope public', scope='public'")));
  }
}