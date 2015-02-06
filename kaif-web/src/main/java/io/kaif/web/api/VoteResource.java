package io.kaif.web.api;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.zone.Zone;
import io.kaif.service.VoteService;

@RestController
@RequestMapping("/api/vote")
public class VoteResource {

  static class UpVoteArticle {
    @NotNull
    public FlakeId articleId;

    @NotNull
    public Zone zone;

    @NotNull
    public Long previousCount;
  }

  @Autowired
  private VoteService voteService;

  @RequestMapping(value = "/article", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void upVoteArticle(AccountAccessToken token, @Valid @RequestBody UpVoteArticle request) {
    voteService.upVoteArticle(request.zone,
        request.articleId,
        token.getAccountId(),
        request.previousCount);
  }

}
