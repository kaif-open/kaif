package io.kaif.service;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Authorization;
import io.kaif.model.article.Article;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateTree;
import io.kaif.model.zone.Zone;

public interface ArticleService {

  Article createExternalLink(Authorization author, Zone zone, String title, String link);

  List<Article> listLatestZoneArticles(Zone zone, @Nullable FlakeId startArticleId);

  Article loadArticle(FlakeId articleId);

  Optional<Article> findArticle(FlakeId articleId);

  Debate debate(FlakeId articleId,
      @Nullable FlakeId parentDebateId,
      Authorization debater,
      String content);

  DebateTree listBestDebates(FlakeId articleId, @Nullable FlakeId parentDebateId);

  List<Article> listHotZoneArticles(Zone zone, FlakeId startArticleId);

  List<Article> listLatestArticles(@Nullable FlakeId startArticleId);

  List<Article> listRssHotZoneArticlesWithCache(Zone zone);

  List<Article> listRssTopArticlesWithCache();

  List<Article> listTopArticles(@Nullable FlakeId startArticleId);

  String loadEditableDebateContent(FlakeId debateId, Authorization editor);

  String updateDebateContent(FlakeId debateId, Authorization editor, String content);

  Debate loadDebateWithoutCache(FlakeId debateId);

  boolean canCreateArticle(Zone zone, Authorization author);

  Article createSpeak(Authorization authorization, Zone zone, String title, String content);

  Debate loadDebateWithCache(FlakeId debateId);

  List<Debate> listReplyToDebates(Authorization authorization, @Nullable FlakeId startDebateId);

  List<Debate> listLatestDebates(@Nullable FlakeId startDebateId);

  List<Debate> listLatestZoneDebates(Zone zone, @Nullable FlakeId startDebateId);

  List<Article> listArticlesByDebatesWithCache(List<FlakeId> debateIds);

  List<Debate> listDebatesByIdWithCache(List<FlakeId> debateIds);

  List<Article> listArticlesByAuthor(String username, @Nullable FlakeId startArticleId);

  List<Debate> listDebatesByDebater(String username, @Nullable FlakeId startDebateId);

  boolean isExternalLinkExist(Zone zone, String externalLink);

  List<Article> listArticlesByExternalLink(Zone zone, String externalLink);
}
