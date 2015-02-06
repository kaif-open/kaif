package io.kaif.model.article;

import static java.util.stream.Collectors.*;

import java.util.Comparator;
import java.util.List;

import io.kaif.flake.FlakeId;

public class ArticlePage {
  private final List<Article> articles;
  private final FlakeId startArticleId;
  private final FlakeId endArticleId;

  public ArticlePage(List<Article> articles) {
    this.articles = articles;
    if (articles.isEmpty()) {
      this.startArticleId = FlakeId.MIN;
      this.endArticleId = FlakeId.MIN;
    } else {
      List<Article> sorted = articles.stream()
          .sorted(Comparator.comparing(Article::getArticleId))
          .collect(toList());
      this.startArticleId = sorted.get(0).getArticleId();
      this.endArticleId = sorted.get(sorted.size() - 1).getArticleId();
    }
  }

  public List<Article> getArticles() {
    return articles;
  }

  public FlakeId getStartArticleId() {
    return startArticleId;
  }

  public FlakeId getEndArticleId() {
    return endArticleId;
  }
}
