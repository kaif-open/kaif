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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import io.kaif.flake.FlakeId;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.model.vote.ArticleVoter;
import io.kaif.model.vote.DebateVoter;
import io.kaif.model.vote.VoteState;
import io.kaif.service.ArticleService;
import io.kaif.service.VoteService;
import io.kaif.web.v1.dto.V1VoteDto;

@Api(tags = "vote", description = "Vote on articles or debates")
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

  @ApiOperation(value = "[vote] List votes of the user on multiple articles",
      notes = "List votes of the user on multiple articles. When you paging articles, "
          + "you may want to know what user voted for those articles in the page. "
          + "You can send multiple articleIds to obtain votes in batch.")
  @RequiredScope(VOTE)
  @RequestMapping(value = "/article", method = RequestMethod.GET)
  public List<V1VoteDto> votesOfArticles(ClientAppUserAccessToken token,
      @ApiParam(value = "comma separated articleIds", allowMultiple = true)
      @RequestParam("article-id") List<String> articleIds) {
    return voteService.listArticleVoters(token, toFlakeIds(articleIds))
        .stream()
        .map(ArticleVoter::toV1Dto)
        .collect(toList());
  }

  @ApiOperation(value = "[vote] List votes of the user on multiple debates",
      notes = "List votes of the user on multiple debates. When you paging debates, "
          + "you may want to know what user voted for those debates in the page. "
          + "You can send multiple debateIds to obtain votes in batch.")
  @RequiredScope(VOTE)
  @RequestMapping(value = "/debate", method = RequestMethod.GET)
  public List<V1VoteDto> votesOfDebates(ClientAppUserAccessToken token,
      @ApiParam(value = "comma separated debateIds", allowMultiple = true)
      @RequestParam("debate-id") List<String> debateIds) {
    return voteService.listDebateVotersByIds(token, toFlakeIds(debateIds))
        .stream()
        .map(DebateVoter::toV1Dto)
        .collect(toList());
  }

  @ApiOperation(value = "[vote] List all votes of the user for all debates in an article",
      notes =
          "List all votes of the user on all debates of the article, this is recommend method when "
              + "you want all votes of the user in a large debate tree.")
  @RequiredScope(VOTE)
  @RequestMapping(value = "/debate/article/{articleId}", method = RequestMethod.GET)
  public List<V1VoteDto> votesOfDebatesOfArticle(ClientAppUserAccessToken token,
      @PathVariable("articleId") FlakeId articleId) {
    return voteService.listDebateVoters(token, articleId)
        .stream()
        .map(DebateVoter::toV1Dto)
        .collect(toList());
  }

  @ApiOperation(value = "[vote] Vote on an article",
      notes = "Vote on an article. Note that kaif do not support DOWN vote on article.")
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

  @ApiOperation(value = "[vote] Vote on a debate",
      notes = "Vote on a debate.")
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
