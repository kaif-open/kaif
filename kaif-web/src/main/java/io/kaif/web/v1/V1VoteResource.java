package io.kaif.web.v1;

import static io.kaif.model.clientapp.ClientAppScope.VOTE;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiModelProperty;

import io.kaif.flake.FlakeId;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.model.vote.ArticleVoter;
import io.kaif.model.vote.DebateVoter;
import io.kaif.model.vote.VoteState;
import io.kaif.service.ArticleService;
import io.kaif.service.VoteService;
import io.kaif.web.v1.dto.V1VoteDto;

@Api(value = "vote", description = "Vote on articles or debates")
@RestController
@RequestMapping(value = "/v1/vote", produces = MediaType.APPLICATION_JSON_VALUE)
public class V1VoteResource {

  static class VoteArticleEntry {
    @ApiModelProperty(required = true)
    @NotNull
    public FlakeId articleId;

    @ApiModelProperty(value = "note that articles only support EMPTY and UP state", required = true)
    @NotNull
    public VoteState voteState;
  }

  static class VoteDebateEntry {

    @ApiModelProperty(required = true)
    @NotNull
    public FlakeId debateId;

    @ApiModelProperty(required = true)
    @NotNull
    public VoteState voteState;
  }

  private static List<FlakeId> toFlakeIds(List<String> ids) {
    return Optional.ofNullable(ids)
        .orElse(Collections.emptyList())
        .stream()
        .map(FlakeId::fromString)
        .collect(toList());
  }

  @Autowired
  private VoteService voteService;
  @Autowired
  private ArticleService articleService;

  @RequiredScope(VOTE)
  @RequestMapping(value = "/article", method = RequestMethod.GET)
  public List<V1VoteDto> article(ClientAppUserAccessToken token,
      @RequestParam("article-id") List<String> articleIds) {
    return voteService.listArticleVoters(token, toFlakeIds(articleIds))
        .stream()
        .map(ArticleVoter::toV1Dto)
        .collect(toList());
  }

  @RequiredScope(VOTE)
  @RequestMapping(value = "/debate", method = RequestMethod.GET)
  public List<V1VoteDto> debate(ClientAppUserAccessToken token,
      @RequestParam("debate-id") List<String> debateIds) {
    return voteService.listDebateVotersByIds(token, toFlakeIds(debateIds))
        .stream()
        .map(DebateVoter::toV1Dto)
        .collect(toList());
  }

  @RequiredScope(VOTE)
  @RequestMapping(value = "/debate/article/{articleId}", method = RequestMethod.GET)
  public List<V1VoteDto> debateOfArticle(ClientAppUserAccessToken token,
      @PathVariable("articleId") FlakeId articleId) {
    return voteService.listDebateVoters(token, articleId)
        .stream()
        .map(DebateVoter::toV1Dto)
        .collect(toList());
  }

  @RequiredScope(VOTE)
  @RequestMapping(value = "/article", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void article(ClientAppUserAccessToken token, @Valid @RequestBody VoteArticleEntry entry) {
    ignoreDuplicateVote(() -> {
      VoteState previousState = voteService.listArticleVoters(token, asList(entry.articleId))
          .stream()
          .map(ArticleVoter::getVoteState)
          .findAny()
          .orElse(VoteState.EMPTY);

      //for api, we don't hack previousCount. this may cause user see stale total counting
      int previousCount = 0;
      voteService.voteArticle(entry.voteState,
          entry.articleId,
          token,
          previousState,
          previousCount);
    });
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
    ignoreDuplicateVote(() -> {
      VoteState previousState = voteService.listDebateVotersByIds(token, asList(entry.debateId))
          .stream()
          .map(DebateVoter::getVoteState)
          .findAny()
          .orElse(VoteState.EMPTY);

      //for api, we don't hack previousCount. this may cause user see stale total counting
      int previousCount = 0;
      voteService.voteDebate(entry.voteState, entry.debateId, token, previousState, previousCount);
    });
  }
}
