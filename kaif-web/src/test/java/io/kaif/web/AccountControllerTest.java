package io.kaif.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import org.junit.Test;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.test.MvcIntegrationTests;

public class AccountControllerTest extends MvcIntegrationTests {

  @Test
  public void settingsPart() throws Exception {
    Account account = accountTourist("foo");
    String token = prepareAccessToken(account);

    when(accountService.findById(account.getAccountId())).thenReturn(Optional.of(account));

    mockMvc.perform(get("/account/settings.part").header(AccountAccessToken.HEADER_KEY, token))
        .andExpect(view().name("account/settings.part"))
        .andExpect(content().string(containsString("foo@example.com")))
        .andExpect(content().string(containsString("foo")));
  }

  @Test
  public void activation() throws Exception {
    when(accountService.activate("abc")).thenReturn(true);

    mockMvc.perform(get("/account/activation?key=abc"))
        .andExpect(view().name("account/activation"))
        .andExpect(model().attribute("success", true))
        .andExpect(cookie().secure("force-logout", true))
        .andExpect(cookie().path("force-logout", "/"))
        .andExpect(cookie().value("force-logout", "true"));
  }
}