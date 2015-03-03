package io.kaif.model.debate;

import java.util.List;

import io.kaif.model.article.Article;

public class DebateList {
  private final List<Debate> debates;
  private final List<Article> articles;

  public DebateList(List<Debate> debates, List<Article> articles) {
    this.debates = debates;
    this.articles = articles;
  }

  public List<Debate> getDebates() {
    return debates;
  }

  public Article getArticle(Debate debate) {
    return articles.stream()
        .filter(a -> a.getArticleId().equals(debate.getArticleId()))
        .findAny()
        .get();
  }
}
