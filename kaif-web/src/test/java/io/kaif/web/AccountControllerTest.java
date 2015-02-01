package io.kaif.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.account.Authority;
import io.kaif.test.MvcIntegrationTests;

public class AccountControllerTest extends MvcIntegrationTests {

  @Test
  public void settingsPart() throws Exception {
    AccountAccessToken token = new AccountAccessToken(UUID.randomUUID(),
        "pw",
        EnumSet.allOf(Authority.class));
    when(accountService.tryDecodeAccessToken("a-token")).thenReturn(Optional.of(token));

    Account account = accountTourist("foo");

    when(accountService.findById(token.getAccountId())).thenReturn(Optional.of(account));

    mockMvc.perform(get("/account/settings.part").header("X-KAIF-ACCESS-TOKEN", "a-token"))
        .andExpect(view().name("account/settings.part"))
        .andExpect(content().string(containsString("foo@example.com")))
        .andExpect(content().string(containsString("foo")));
  }
}