package io.kaif.service;

import java.util.List;

import javax.annotation.Nullable;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Authorization;
import io.kaif.model.article.Article;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateTree;
import io.kaif.model.zone.Zone;

public interface ArticleService {

  Article createExternalLink(Authorization author, Zone zone, String title, String url);

  List<Article> listLatestZoneArticles(Zone zone, @Nullable FlakeId startArticleId);

  Article loadArticle(FlakeId articleId);

  Debate debate(Zone zone,
      FlakeId articleId,
      @Nullable FlakeId parentDebateId,
      Authorization debater,
      String content);

  DebateTree listBestDebates(FlakeId articleId, @Nullable FlakeId parentDebateId);

  List<Article> listHotZoneArticles(Zone zone, FlakeId startArticleId);

  List<Article> listLatestArticles(@Nullable FlakeId startArticleId);

  List<Article> listTopArticles(@Nullable FlakeId startArticleId);

  String loadEditableDebateContent(FlakeId debateId, Authorization editor);

  String updateDebateContent(FlakeId debateId, Authorization editor, String content);

  Debate loadDebate(FlakeId debateId);

  boolean canCreateArticle(Zone zone, Authorization auth);
}
