package io.kaif.web.v1;

import static io.kaif.model.clientapp.ClientAppScope.VOTE;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;

import io.kaif.flake.FlakeId;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.model.vote.VoteState;

@Api(value = "vote", description = "Vote on articles or debates")
@RestController
@RequestMapping(value = "/v1/vote", produces = MediaType.APPLICATION_JSON_VALUE)
public class V1VoteResource {

  static class VoteArticleEntry {
    @NotNull
    public FlakeId articleId;

    //TODO document passing down vote will cause exception
    @NotNull
    public VoteState state;
  }

  static class VoteDebateEntry {
    @NotNull
    public FlakeId debateId;

    @NotNull
    public VoteState state;
  }

  @RequiredScope(VOTE)
  @RequestMapping(value = "/article", method = RequestMethod.GET)
  public void article(ClientAppUserAccessToken token,
      @RequestParam("article-id") List<String> articleIds) {
  }

  @RequiredScope(VOTE)
  @RequestMapping(value = "/debate", method = RequestMethod.GET)
  public void debate(ClientAppUserAccessToken token,
      @RequestParam("debate-id") List<String> debateIds) {
  }

  @RequiredScope(VOTE)
  @RequestMapping(value = "/debate/article/{articleId}", method = RequestMethod.GET)
  public void debateOfArticle(ClientAppUserAccessToken token) {
  }

  @RequiredScope(VOTE)
  @RequestMapping(value = "/article", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void article(ClientAppUserAccessToken token, @Valid @RequestBody VoteArticleEntry entry) {
    ignoreDuplicateVote(null);
  }

  private void ignoreDuplicateVote(Runnable runnable) {
    try {
      runnable.run();
    } catch (DuplicateKeyException ignore) {
      // user duplicate vote, this mostly happened when user press browser back.
      // this typically is fine, we safely ignore
    }
  }

  @RequiredScope(VOTE)
  @RequestMapping(value = "/debate", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void debate(ClientAppUserAccessToken token, @Valid @RequestBody VoteDebateEntry entry) {
    ignoreDuplicateVote(null);
  }
}
