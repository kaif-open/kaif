package io.kaif.model.article;

import static java.util.stream.Collectors.*;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import io.kaif.flake.FlakeId;

public class ArticleList {
  private final List<Article> articles;
  private final FlakeId oldestArticleId;
  private final FlakeId newestArticleId;

  public ArticleList(List<Article> articles) {
    this.articles = articles;
    if (articles.isEmpty()) {
      this.oldestArticleId = FlakeId.MIN;
      this.newestArticleId = FlakeId.MIN;
    } else {
      List<Article> sorted = articles.stream()
          .sorted(Comparator.comparing(Article::getArticleId))
          .collect(toList());
      this.oldestArticleId = sorted.get(0).getArticleId();
      this.newestArticleId = sorted.get(sorted.size() - 1).getArticleId();
    }
  }

  public List<Article> getArticles() {
    return articles;
  }

  public FlakeId getOldestArticleId() {
    return oldestArticleId;
  }

  public FlakeId getNewestArticleId() {
    return newestArticleId;
  }

  public boolean hasNext() {
    return !articles.isEmpty();
  }

  @Nullable
  public FlakeId getLastArticleId() {
    return hasNext() ? articles.get(articles.size() - 1).getArticleId() : null;
  }
}
