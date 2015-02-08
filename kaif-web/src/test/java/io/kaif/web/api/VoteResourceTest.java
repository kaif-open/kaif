package io.kaif.web.api;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.account.Authorization;
import io.kaif.model.vote.VoteState;
import io.kaif.model.zone.Zone;
import io.kaif.test.MvcIntegrationTests;

public class VoteResourceTest extends MvcIntegrationTests {
  @Test
  public void ignoreDuplicateVote() throws Exception {
    Account account = accountCitizen("foo");
    String token = prepareAccessToken(account);

    VoteResource.VoteArticle voteArticle = new VoteResource.VoteArticle();
    voteArticle.articleId = FlakeId.fromString("a");
    voteArticle.newState = VoteState.DOWN;
    voteArticle.previousCount = 100L;
    voteArticle.previousState = VoteState.EMPTY;
    voteArticle.zone = Zone.valueOf("abc");

    Mockito.doThrow(new DuplicateKeyException("vote dup"))
        .when(voteService)
        .voteArticle(eq(VoteState.DOWN),
            eq(Zone.valueOf("abc")),
            eq(FlakeId.fromString("a")),
            isA(Authorization.class),
            eq(VoteState.EMPTY),
            eq(100L));

    mockMvc.perform(post("/api/vote/article").header(AccountAccessToken.HEADER_KEY, token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsBytes(voteArticle))).andExpect(status().isOk());
  }
}