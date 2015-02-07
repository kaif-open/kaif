package io.kaif.web.api;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.vote.ArticleVoter;
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

  static class CancelVoteArticle {
    @NotNull
    public FlakeId articleId;

    @NotNull
    public Zone zone;
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

  @RequestMapping(value = "/article-canel", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void cancelVoteArticle(AccountAccessToken token,
      @Valid @RequestBody CancelVoteArticle request) {
    voteService.cancelVoteArticle(request.zone, request.articleId, token.getAccountId());
  }

  @RequestMapping(value = "/article-voters", method = RequestMethod.GET)
  public List<ArticleVoter> listArticleVotersInRage(AccountAccessToken token,
      @RequestParam("startArticleId") String startArticleId,
      @RequestParam("endArticleId") String endArticleId) {
    //TODO ArticleVoterDto ?
    return voteService.listArticleVotersInRage(token.getAccountId(),
        FlakeId.fromString(startArticleId),
        FlakeId.fromString(endArticleId));
  }
}
