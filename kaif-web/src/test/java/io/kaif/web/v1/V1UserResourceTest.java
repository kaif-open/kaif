package io.kaif.web.v1;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;

import io.kaif.model.account.Account;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.test.MvcIntegrationTests;

public class V1UserResourceTest extends MvcIntegrationTests {

  @Test
  @Ignore
  public void printApiDocs() throws Exception {
    mockMvc.perform(get("/api-docs/v1/user")).andDo(print());
  }

  @Test
  public void basic() throws Exception {
    Instant createTime = ZonedDateTime.of(2015, 3, 4, 23, 5, 6, 7, ZoneId.of("Asia/Taipei"))
        .toInstant();
    Account account = Account.create("user1", "user1@example.com", "pwd", createTime);
    when(accountService.findMe(isA(ClientAppUserAccessToken.class))).thenReturn(Optional.of(account));
    oauthPerform(account, get("/v1/user/basic")).andExpect(status().isOk())
        .andExpect(jsonPath("$.data.username", is("user1")))
        .andExpect(jsonPath("$.data.createTime", is("2015-03-04T15:05:06Z")));
  }

}