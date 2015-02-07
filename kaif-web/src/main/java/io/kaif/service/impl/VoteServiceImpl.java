package io.kaif.service.impl;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Authorization;
import io.kaif.model.article.ArticleDao;
import io.kaif.model.vote.ArticleVoter;
import io.kaif.model.vote.VoteDao;
import io.kaif.model.vote.VoteDelta;
import io.kaif.model.zone.Zone;
import io.kaif.service.VoteService;

@Service
@Transactional
public class VoteServiceImpl implements VoteService {

  @Autowired
  private ArticleDao articleDao;

  @Autowired
  private VoteDao voteDao;

  @Override
  public void upVoteArticle(Zone zone,
      FlakeId articleId,
      Authorization authorization,
      long previousCount) {

    //relax article up vote verification, no check zone and account in Database

    VoteDelta voteDelta = voteDao.upVoteArticle(articleId,
        authorization.authenticatedId(),
        previousCount,
        Instant.now());
    articleDao.changeUpVote(zone, articleId, voteDelta.getChangedValue());
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

    //relax article cancel vote verification, no check zone and account in Database

    VoteDelta voteDelta = voteDao.cancelVoteArticle(articleId,
        authorization.authenticatedId(),
        Instant.now());
    articleDao.changeUpVote(zone, articleId, voteDelta.getChangedValue());
  }
}
