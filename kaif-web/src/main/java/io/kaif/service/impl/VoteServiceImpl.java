package io.kaif.service.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Authorization;
import io.kaif.model.article.ArticleDao;
import io.kaif.model.debate.DebateDao;
import io.kaif.model.vote.ArticleVoter;
import io.kaif.model.vote.DebateVoter;
import io.kaif.model.vote.VoteDao;
import io.kaif.model.vote.VoteDelta;
import io.kaif.model.vote.VoteState;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneDao;
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

  @Override
  public void upVoteArticle(Zone zone,
      FlakeId articleId,
      Authorization authorization,
      long previousCount) {

    checkVoteAuthority(zone, authorization);

    VoteDelta voteDelta = voteDao.upVoteArticle(articleId,
        authorization.authenticatedId(),
        previousCount,
        Instant.now());
    articleDao.changeTotalVote(zone, articleId, voteDelta.getChangedValue());
  }

  private void checkVoteAuthority(Zone zone, Authorization authorization) {
    //relax article up vote verification, no check zone and account in Database
    if (!zoneDao.loadZone(zone).canUpVote(authorization)) {
      throw new AccessDeniedException("not allow vote in zone: " + zone + " auth:" + authorization);
    }
  }

  @Override
  public List<ArticleVoter> listArticleVotersInRage(Authorization authorization,
      FlakeId startArticleId,
      FlakeId endArticleId) {
    return voteDao.listArticleVotersInRage(authorization.authenticatedId(),
        startArticleId,
        endArticleId);
  }

  @Override
  public void cancelVoteArticle(Zone zone, FlakeId articleId, Authorization authorization) {

    checkVoteAuthority(zone, authorization);

    VoteDelta voteDelta = voteDao.cancelVoteArticle(articleId,
        authorization.authenticatedId(),
        Instant.now());
    articleDao.changeTotalVote(zone, articleId, voteDelta.getChangedValue());
  }

  public void upVoteDebate(Zone zone,
      FlakeId articleId,
      FlakeId debateId,
      Authorization voter,
      int previousCount,
      VoteState previousState) {
    checkVoteAuthority(zone, voter);

    voteDao.upVotedDebate(articleId,
        debateId,
        voter.authenticatedId(),
        previousCount,
        Instant.now());

    VoteDelta upVoteDelta = VoteState.UP.upVoteDelta(previousState);
    VoteDelta downVoteDelta = VoteState.UP.downVoteDelta(previousState);
    debateDao.changeTotalVote(articleId,
        debateId,
        upVoteDelta.getChangedValue(),
        downVoteDelta.getChangedValue());
  }

  public List<DebateVoter> listDebateVoters(Authorization voter, FlakeId articleId) {
    return voteDao.listDebateVotersByArticle(voter.authenticatedId(), articleId);
  }

  public void downVoteDebate(Zone zone,
      FlakeId articleId,
      FlakeId debateId,
      Authorization voter,
      long previousCount,
      VoteState previousState) {
    checkVoteAuthority(zone, voter);

    voteDao.downVotedDebate(articleId,
        debateId,
        voter.authenticatedId(),
        previousCount,
        Instant.now());
    VoteDelta upVoteDelta = VoteState.DOWN.upVoteDelta(previousState);
    VoteDelta downVoteDelta = VoteState.DOWN.downVoteDelta(previousState);
    debateDao.changeTotalVote(articleId,
        debateId,
        upVoteDelta.getChangedValue(),
        downVoteDelta.getChangedValue());
  }

  public void cancelVoteDebate(Zone zone,
      FlakeId articleId,
      FlakeId debateId,
      Authorization voter,
      VoteState previousState) {
    checkVoteAuthority(zone, voter);

    boolean success = voteDao.cancelVoteDebate(articleId,
        debateId,
        voter.authenticatedId(),
        previousState,
        Instant.now());
    if (success) {
      VoteDelta upVoteDelta = VoteState.EMPTY.upVoteDelta(previousState);
      VoteDelta downVoteDelta = VoteState.EMPTY.downVoteDelta(previousState);
      debateDao.changeTotalVote(articleId,
          debateId,
          upVoteDelta.getChangedValue(),
          downVoteDelta.getChangedValue());
    }
  }
}
