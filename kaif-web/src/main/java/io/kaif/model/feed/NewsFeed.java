package io.kaif.model.feed;

import java.util.List;

import io.kaif.model.article.Article;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateList;

public class NewsFeed {
  private final List<FeedAsset> feedAssets;
  private final DebateList debateList;

  public NewsFeed(List<FeedAsset> feedAssets, List<Debate> debates, List<Article> articles) {
    this.feedAssets = feedAssets;
    this.debateList = new DebateList(debates, articles);
  }

  public List<FeedAsset> getFeedAssets() {
    return feedAssets;
  }

  public Article getArticle(Debate debate) {
    return debateList.getArticle(debate);
  }

  public Debate getDebate(FeedAsset asset) {
    return debateList.getDebates()
        .stream()
        .filter(d -> d.getDebateId().equals(asset.getAssetId()))
        .findAny()
        .get();
  }
}
