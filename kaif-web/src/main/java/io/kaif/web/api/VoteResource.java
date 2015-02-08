package io.kaif.web.api;

import static java.util.stream.Collectors.*;

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
import io.kaif.model.vote.ArticleVoterDto;
import io.kaif.model.vote.DebateVoter;
import io.kaif.model.vote.DebateVoterDto;
import io.kaif.model.vote.VoteState;
import io.kaif.model.zone.Zone;
import io.kaif.service.VoteService;

@RestController
@RequestMapping("/api/vote")
public class VoteResource {

  static class VoteArticle {
    @NotNull
    public FlakeId articleId;

    @NotNull
    public Zone zone;

    @NotNull
    public Long previousCount;

    @NotNull
    public VoteState newState;

    @NotNull
    public VoteState previousState;
  }

  static class VoteDebate {

    @NotNull
    public FlakeId articleId;

    @NotNull
    public FlakeId debateId;

    @NotNull
    public Zone zone;

    @NotNull
    public VoteState newState;

    @NotNull
    public VoteState previousState;

    @NotNull
    public Long previousCount;

  }

  @Autowired
  private VoteService voteService;

  @RequestMapping(value = "/article", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void voteArticle(AccountAccessToken token, @Valid @RequestBody VoteArticle request) {
    voteService.voteArticle(request.newState,
        request.zone,
        request.articleId,
        token,
        request.previousState,
        request.previousCount);
  }

  @RequestMapping(value = "/debate", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void voteDebate(AccountAccessToken token, @Valid @RequestBody VoteDebate request) {
    voteService.voteDebate(request.newState,
        request.zone,
        request.articleId,
        request.debateId,
        token,
        request.previousState,
        request.previousCount);
  }

  @RequestMapping(value = "/article-voters", method = RequestMethod.GET)
  public List<ArticleVoterDto> listArticleVotersInRage(AccountAccessToken token,
      @RequestParam("startArticleId") String startArticleId,
      @RequestParam("endArticleId") String endArticleId) {
    return voteService.listArticleVotersInRage(token,
        FlakeId.fromString(startArticleId),
        FlakeId.fromString(endArticleId)).stream().map(ArticleVoter::toDto).collect(toList());
  }

  @RequestMapping(value = "/debate-voters", method = RequestMethod.GET)
  public List<DebateVoterDto> lisDebateVoters(AccountAccessToken token,
      @RequestParam("articleId") String articleId) {
    return voteService.listDebateVoters(token, FlakeId.fromString(articleId))
        .stream()
        .map(DebateVoter::toDto)
        .collect(toList());
  }
}
