package io.kaif.web.v1;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Locale;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import io.kaif.model.account.Account;
import io.kaif.test.MvcIntegrationTests;

public class V1ResponseWrapperAdviceTest extends MvcIntegrationTests {

  @Test
  public void wrapPrimitive() throws Exception {
    Account account = accountCitizen("foo");
    String token = prepareClientAppUserAccessToken(account);
    mockMvc.perform(get("/v1/echo/current-time")//
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON))
        //.andDo(print())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", isA(Long.class)));
  }

  @Test
  public void wrapString() throws Exception {
    Account account = accountCitizen("foo");
    String token = prepareClientAppUserAccessToken(account);
    mockMvc.perform(post("/v1/echo/message")//
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .content(q("{'message':'abc'}"))
        .contentType(MediaType.APPLICATION_JSON))
        //.andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType("application/json;charset=UTF-8"))
        .andExpect(jsonPath("$.data", is("abc")));
  }

  @Test
  public void errorResponse() throws Exception {
    Account account = accountCitizen("foo");
    String token = prepareClientAppUserAccessToken(account);
    mockMvc.perform(post("/v1/echo/test-failure")//
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .content(q("{'message':'abc'}"))
        .locale(Locale.ENGLISH)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors[0].type", is("RequireCitizenException")))
        .andExpect(jsonPath("$.errors[0].translated", is(true)))
        .andExpect(jsonPath("$.errors[0].title",
            is("You have not activated your account, please activate and try again.")));
  }
}