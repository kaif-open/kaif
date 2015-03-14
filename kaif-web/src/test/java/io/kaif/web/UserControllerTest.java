package io.kaif.web;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountStats;
import io.kaif.model.article.Article;
import io.kaif.model.debate.Debate;
import io.kaif.model.zone.Zone;
import io.kaif.test.MvcIntegrationTests;

public class UserControllerTest extends MvcIntegrationTests {

  Account account = accountCitizen("foo-user");
  Zone zone = Zone.valueOf("programming");

  @Test
  public void userProfile() throws Exception {
    when(accountService.loadAccount("foo-user")).thenReturn(account);
    when(accountService.loadAccountStats("foo-user")).thenReturn(AccountStats.zero(account.getAccountId()));
    mockMvc.perform(get("/u/foo-user"))
        .andExpect(content().string(containsString("關於 foo-user")))
        .andExpect(content().string(containsString(
            "<link rel=\"canonical\" href=\"https://kaif.io/u/foo-user\"/>")));
  }

  @Test
  public void userProfile_redirect() throws Exception {
    when(accountService.loadAccount("Foo-User")).thenReturn(account);
    mockMvc.perform(get("/u/Foo-User"))
        .andExpect(redirectedUrl("/u/foo-user"))
        .andExpect(status().isMovedPermanently());
  }

  @Test
  public void createdArticles() throws Exception {
    Article a1 = article(zone, "ruby is great");
    Article a2 = article(zone, "I like rails");
    when(articleService.listArticlesByAuthor("foo-user", null)).thenReturn(asList(a1, a2));
    mockMvc.perform(get("/u/foo-user/articles"))
        .andExpect(content().string(containsString("I like rails")));
  }

  @Test
  public void createdDebates() throws Exception {
    Article a1 = article(zone, "ruby is great");
    Debate debate = debate(a1, "i think so", null);
    when(articleService.listDebatesByDebater("foo-user", null)).thenReturn(asList(debate));
    when(articleService.listArticlesByDebates(asList(debate.getDebateId()))).thenReturn(asList(a1));
    mockMvc.perform(get("/u/foo-user/debates"))
        .andExpect(content().string(containsString("i think so")))
        .andExpect(containsDebateFormTemplate());
  }
}