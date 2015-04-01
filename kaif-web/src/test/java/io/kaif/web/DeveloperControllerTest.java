package io.kaif.web;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Optional;

import org.junit.Test;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.account.Authorization;
import io.kaif.test.MvcIntegrationTests;

public class DeveloperControllerTest extends MvcIntegrationTests {

  @Test
  public void clientApp() throws Exception {

    //    when(zoneService.listRecommendZones()).thenReturn(//
    //        asList(zoneDefault("zone1"), zoneDefault("zone2")));

    mockMvc.perform(get("/developer/client-app")).andExpect(view().name("developer/client-app"));
  }

  @Test
  public void clientAppPart() throws Exception {
    Account account = accountTourist("foo");
    String token = prepareAccessToken(account);
    when(accountService.findMe(isA(Authorization.class))).thenReturn(Optional.of(account));

    mockMvc.perform(get("/developer/client-app.part").header(AccountAccessToken.HEADER_KEY, token))
        .andExpect(view().name("developer/client-app.part"));
  }
}