package io.kaif.service;

import java.util.List;
import java.util.UUID;

import io.kaif.flake.FlakeId;
import io.kaif.model.vote.ArticleVoter;
import io.kaif.model.zone.Zone;

public interface VoteService {
  void upVoteArticle(Zone zone, FlakeId articleId, UUID accountId, long previousCount);

  List<ArticleVoter> listArticleVotersInRage(UUID accountId,
      FlakeId startArticleId,
      FlakeId endArticleId);
}
