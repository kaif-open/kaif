package io.kaif.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.kaif.flake.FlakeId;
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
  public void upVoteArticle(Zone zone, FlakeId articleId, UUID accountId, long previousCount) {
    VoteDelta voteDelta = voteDao.upVoteArticle(articleId, accountId, previousCount, Instant.now());
    articleDao.changeUpVote(zone, articleId, voteDelta.getChangedValue());
  }

  @Override
  public List<ArticleVoter> listArticleVotersInRage(UUID accountId,
      FlakeId startArticleId,
      FlakeId endArticleId) {
    return voteDao.listArticleVotersInRage(accountId, startArticleId, endArticleId);
  }

  @Override
  public void cancelVoteArticle(Zone zone, FlakeId articleId, UUID accountId) {
    VoteDelta voteDelta = voteDao.cancelVoteArticle(articleId, accountId, Instant.now());
    articleDao.changeUpVote(zone, articleId, voteDelta.getChangedValue());
  }
}
