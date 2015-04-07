package io.kaif.web.v1;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;

import io.kaif.model.account.Account;
import io.kaif.test.MvcIntegrationTests;

public class V1ZoneResourceTest extends MvcIntegrationTests {

  private Account user;

  @Before
  public void setUp() throws Exception {
    user = accountCitizen("user1");
  }

  @Test
  public void all() throws Exception {
    when(zoneService.listCitizenZones()).thenReturn(asList(zoneDefault("xyz")));
    oauthPerform(user, get("/v1/zone/all")).andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].name", is("xyz")));
  }
}