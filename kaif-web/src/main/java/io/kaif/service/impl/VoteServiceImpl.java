package io.kaif.service.impl;

import java.time.Instant;
import java.util.List;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.AccountDao;
import io.kaif.model.account.Authorization;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticleDao;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateDao;
import io.kaif.model.vote.ArticleVoter;
import io.kaif.model.vote.DebateVoter;
import io.kaif.model.vote.HonorRollDao;
import io.kaif.model.vote.HonorRollVoter;
import io.kaif.model.vote.VoteDao;
import io.kaif.model.vote.VoteState;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneDao;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.service.VoteService;
import io.kaif.web.support.AccessDeniedException;

@Service
@Transactional
public class VoteServiceImpl implements VoteService {

  @Autowired
  private ArticleDao articleDao;

  @Autowired
  private VoteDao voteDao;

  @Autowired
  private ZoneDao zoneDao;

  @Autowired
  private DebateDao debateDao;

  @Autowired
  private AccountDao accountDao;

  @Autowired
  private HonorRollDao honorRollDao;

  private ZoneInfo checkVoteAuthority(Zone zone, Authorization authorization) {
    // relax verification when voting, no check zone and account in Database because voting
    // is not critical
    ZoneInfo zoneInfo = zoneDao.loadZoneWithCache(zone);
    if (!zoneInfo.canUpVote(authorization)) {
      throw new AccessDeniedException("not allow vote in zone: " + zone + " auth:" + authorization);
    }
    return zoneInfo;
  }

  @Override
  public List<DebateVoter> listDebateVoters(Authorization voter, FlakeId articleId) {
    return voteDao.listDebateVotersByArticle(voter.authenticatedId(), articleId);
  }

  @Override
  public void voteArticle(VoteState newState,
      FlakeId articleId,
      Authorization authorization,
      VoteState previousState,
      long previousCount) {

    //no support down vote yet
    Preconditions.checkArgument(newState != VoteState.DOWN);

    // note that cached article may have stale data (such as not yet change total vote yet),
    // despite above code already change it.
    Article cachedArticle = articleDao.loadArticleWithCache(articleId);

    ZoneInfo zoneInfo = checkVoteAuthority(cachedArticle.getZone(), authorization);

    voteDao.voteArticle(newState,
        articleId,
        authorization.authenticatedId(),
        previousState,
        previousCount,
        Instant.now());

    int upVoteDelta = newState.upVoteDeltaFrom(previousState);
    int downVoteDelta = newState.downVoteDeltaFrom(previousState);

    articleDao.changeTotalVote(articleId, upVoteDelta, downVoteDelta);

    if (zoneInfo.canContributeVoteStats() && !authorization.authenticatedId()
        .equals(cachedArticle.getAuthorId())) {
      accountDao.changeTotalVotedArticle(cachedArticle.getAuthorId(), upVoteDelta, downVoteDelta);
      honorRollDao.updateRotateVoteStats(HonorRollVoter.createByVote(cachedArticle,
          upVoteDelta,
          downVoteDelta));
    }
  }

  @Override
  public void voteDebate(VoteState newState,
      FlakeId debateId,
      Authorization voter,
      VoteState previousState,
      long previousCount) {

    // note: cached debate may have stale data, such total vote not change yet
    Debate cachedDebate = debateDao.loadDebateWithCache(debateId);

    ZoneInfo zoneInfo = checkVoteAuthority(cachedDebate.getZone(), voter);

    voteDao.voteDebate(newState,
        cachedDebate.getArticleId(),
        debateId,
        voter.authenticatedId(),
        previousState,
        previousCount,
        Instant.now());

    int upVoteDelta = newState.upVoteDeltaFrom(previousState);
    int downVoteDelta = newState.downVoteDeltaFrom(previousState);

    debateDao.changeTotalVote(debateId, upVoteDelta, downVoteDelta);

    // total debate vote score only count citizen zone. (tourist zone like /z/test
    // or kVoting will not count)
    if (zoneInfo.canContributeVoteStats() && !voter.authenticatedId()
        .equals(cachedDebate.getDebaterId())) {
      accountDao.changeTotalVotedDebate(cachedDebate.getDebaterId(), upVoteDelta, downVoteDelta);
      honorRollDao.updateRotateVoteStats(HonorRollVoter.createByVote(cachedDebate,
          upVoteDelta,
          downVoteDelta));
    }
  }

  @Override
  public List<ArticleVoter> listArticleVoters(Authorization voter, List<FlakeId> articleIds) {
    return voteDao.listArticleVoters(voter.authenticatedId(), articleIds);
  }

  @Override
  public List<DebateVoter> listDebateVotersByIds(Authorization voter, List<FlakeId> debateIds) {
    return voteDao.listDebateVotersByIds(voter.authenticatedId(), debateIds);
  }

  @Override
  public List<Article> listUpVotedArticles(Authorization voter, @Nullable FlakeId startArticleId) {
    return voteDao.listUpVotedArticles(voter.authenticatedId(),
        startArticleId,
        ArticleServiceImpl.PAGE_SIZE);
  }
}
