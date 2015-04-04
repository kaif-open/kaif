package io.kaif.web.v1;

import static org.hamcrest.CoreMatchers.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import io.kaif.model.account.Account;
import io.kaif.test.MvcIntegrationTests;

public class V1EchoResourceTest extends MvcIntegrationTests {
  @Test
  public void currentTime() throws Exception {
    Account account = accountCitizen("foo");
    String token = prepareClientAppUserAccessToken(account);
    mockMvc.perform(get("/v1/echo/current-time")//
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON))
        //.andDo(print())
        .andExpect(status().isOk()).andExpect(jsonPath("$.data", isA(Long.class)));
  }
}