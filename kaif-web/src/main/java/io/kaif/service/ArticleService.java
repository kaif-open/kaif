package io.kaif.service;

import java.util.UUID;

import io.kaif.model.article.Article;
import io.kaif.model.zone.Zone;

public interface ArticleService {
  Article createExternalLink(UUID accountId, Zone zone, String title, String url);
}
