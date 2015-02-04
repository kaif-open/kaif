package io.kaif.service;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import io.kaif.flake.FlakeId;
import io.kaif.model.article.Article;
import io.kaif.model.debate.Debate;
import io.kaif.model.zone.Zone;

public interface ArticleService {

  Article createExternalLink(UUID accountId, Zone zone, String title, String url);

  List<Article> listLatestArticles(Zone zone, int page);

  Article loadArticle(Zone zone, FlakeId articleId);

  Debate debate(Zone zone,
      FlakeId articleId,
      @Nullable FlakeId parentDebateId,
      UUID debaterId,
      String content);

  List<Debate> listHotDebates(Zone zone, FlakeId articleId, int offset);
}
