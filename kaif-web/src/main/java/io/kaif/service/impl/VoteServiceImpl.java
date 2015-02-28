package io.kaif.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.AccountDao;
import io.kaif.model.account.Authority;
import io.kaif.model.account.Authorization;
import io.kaif.model.article.ArticleDao;
import io.kaif.model.debate.DebateDao;
import io.kaif.model.vote.ArticleVoter;
import io.kaif.model.vote.DebateVoter;
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

  private ZoneInfo checkVoteAuthority(Zone zone, Authorization authorization) {
    // relax verification when voting, no check zone and account in Database because voting
    // is not critical
    ZoneInfo zoneInfo = zoneDao.loadZone(zone);
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
      Zone zone,
      FlakeId articleId,
      Authorization authorization,
      VoteState previousState,
      long previousCount) {

    //no support down vote yet
    Preconditions.checkArgument(newState != VoteState.DOWN);

    checkVoteAuthority(zone, authorization);

    voteDao.voteArticle(newState,
        articleId,
        authorization.authenticatedId(),
        previousState,
        previousCount,
        Instant.now());

    int upVoteDelta = newState.upVoteDeltaFrom(previousState);
    int downVoteDelta = newState.downVoteDeltaFrom(previousState);
    articleDao.changeTotalVote(articleId, upVoteDelta, downVoteDelta);
  }

  @Override
  public void voteDebate(VoteState newState,
      Zone zone,
      FlakeId articleId,
      FlakeId debateId,
      Authorization voter,
      VoteState previousState,
      long previousCount) {

    ZoneInfo zoneInfo = checkVoteAuthority(zone, voter);

    voteDao.voteDebate(newState,
        articleId,
        debateId,
        voter.authenticatedId(),
        previousState,
        previousCount,
        Instant.now());

    int upVoteDelta = newState.upVoteDeltaFrom(previousState);
    int downVoteDelta = newState.downVoteDeltaFrom(previousState);

    debateDao.changeTotalVote(debateId, upVoteDelta, downVoteDelta);

    UUID debaterId = debateDao.loadDebaterId(debateId);
    // total debate vote score only count citizen zone. (tourist zone like /z/test
    // or kVoting will not count)
    if (zoneInfo.getVoteAuthority() == Authority.CITIZEN) {
      accountDao.changeTotalVotedDebate(debaterId, upVoteDelta, downVoteDelta);
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
}
