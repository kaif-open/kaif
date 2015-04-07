package io.kaif.web;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import org.junit.Test;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.account.Authorization;
import io.kaif.model.clientapp.ClientApp;
import io.kaif.test.MvcIntegrationTests;

public class DeveloperControllerTest extends MvcIntegrationTests {

  @Test
  public void clientApp() throws Exception {
    mockMvc.perform(get("/developer/client-app")).andExpect(view().name("developer/client-app"));
  }

  @Test
  public void clientAppPart() throws Exception {
    Account account = accountTourist("foo");
    String token = prepareAccessToken(account);
    when(accountService.findMe(isA(Authorization.class))).thenReturn(Optional.of(account));
    ClientApp app1 = clientApp(account, "app1");
    ClientApp app2 = clientApp(account, "app2");
    when(clientAppService.listClientApps(isA(Authorization.class))).thenReturn(asList(app1, app2));
    mockMvc.perform(get("/developer/client-app.part").header(AccountAccessToken.HEADER_KEY, token))
        .andExpect(view().name("developer/client-app.part"))
        .andExpect(containsText("app1"))
        .andExpect(containsText("app2"));
  }
}