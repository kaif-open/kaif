package io.kaif.service;

import java.util.List;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Authorization;
import io.kaif.model.vote.ArticleVoter;
import io.kaif.model.vote.DebateVoter;
import io.kaif.model.vote.VoteState;
import io.kaif.model.zone.Zone;

public interface VoteService {

  void voteArticle(VoteState newState,
      Zone zone,
      FlakeId articleId,
      Authorization authorization,
      VoteState previousState,
      long previousCount);

  List<ArticleVoter> listArticleVotersInRage(Authorization authorization,
      FlakeId oldestArticleId,
      FlakeId newestArticleId);

  List<DebateVoter> listDebateVoters(Authorization voter, FlakeId articleId);

  void voteDebate(VoteState newState,
      Zone zone,
      FlakeId articleId,
      FlakeId debateId,
      Authorization voter,
      VoteState previousState,
      long previousCount);

}
