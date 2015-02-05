package io.kaif.service.impl;

import java.time.Duration;
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

  public void upVoteArticle(Zone zone, FlakeId articleId, UUID accountId, long previousCount) {
    VoteDelta voteDelta = voteDao.upVoteArticle(articleId, accountId, previousCount, Instant.now());
    articleDao.changeUpVote(zone, articleId, voteDelta.getChangedValue());
  }

  public List<ArticleVoter> listRecentArticleVotes(UUID accountId) {
    Instant ago = Instant.now().minus(Duration.ofMinutes(10));
    return voteDao.listArticleVotersAfter(accountId, ago);
  }
}
