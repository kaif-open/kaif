package io.kaif.web.api;

import static java.util.stream.Collectors.*;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
import io.kaif.service.VoteService;

@RestController
@RequestMapping("/api/vote")
public class VoteResource {

  static class VoteArticle {
    @NotNull
    public FlakeId articleId;

    @NotNull
    public Long previousCount;

    @NotNull
    public VoteState newState;

    @NotNull
    public VoteState previousState;
  }

  static class VoteDebate {

    @NotNull
    public FlakeId debateId;

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
    ignoreDuplicateVote(() -> //
        voteService.voteArticle(request.newState,
            request.articleId,
            token,
            request.previousState,
            request.previousCount));
  }

  private void ignoreDuplicateVote(Runnable runnable) {
    try {
      runnable.run();
    } catch (DuplicateKeyException ignore) {
      // user duplicate vote, this mostly happened when user press browser back.
      // this typically is fine, we safely ignore
    }
  }

  @RequestMapping(value = "/debate", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void voteDebate(AccountAccessToken token, @Valid @RequestBody VoteDebate request) {
    ignoreDuplicateVote(() -> //
        voteService.voteDebate(request.newState,
            request.debateId,
            token,
            request.previousState,
            request.previousCount));
  }

  @RequestMapping(value = "/article-voters", method = RequestMethod.GET)
  public List<ArticleVoterDto> listArticleVoters(AccountAccessToken token,
      @RequestParam("articleIds") List<String> articleIds) {
    List<FlakeId> flakeIds = articleIds.stream().map(FlakeId::fromString).collect(toList());
    return voteService.listArticleVoters(token, flakeIds)
        .stream()
        .map(ArticleVoter::toDto)
        .collect(toList());
  }

  @RequestMapping(value = "/debate-voters", method = RequestMethod.GET)
  public List<DebateVoterDto> lisDebateVoters(AccountAccessToken token,
      @RequestParam("articleId") FlakeId articleId) {
    return voteService.listDebateVoters(token, articleId)
        .stream()
        .map(DebateVoter::toDto)
        .collect(toList());
  }

  @RequestMapping(value = "/debate-voters-by-ids", method = RequestMethod.GET)
  public List<DebateVoterDto> lisDebateVoters(AccountAccessToken token,
      @RequestParam("debateIds") List<String> debateIds) {
    List<FlakeId> flakeIds = debateIds.stream().map(FlakeId::fromString).collect(toList());
    return voteService.listDebateVotersByIds(token, flakeIds)
        .stream()
        .map(DebateVoter::toDto)
        .collect(toList());
  }
}
