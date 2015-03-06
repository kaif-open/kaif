package io.kaif.web;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.feed.AbstractRssFeedView;

import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Description;
import com.rometools.rome.feed.rss.Guid;
import com.rometools.rome.feed.rss.Item;

import io.kaif.model.article.Article;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneInfo;

public class HotArticleRssContentView extends AbstractRssFeedView {

  private final static String SCHEME_AND_HOST = "https://kaif.io";

  public HotArticleRssContentView() {
    setContentType("application/rss+xml;charset=UTF-8");
  }

  private static String zoneUrl(Zone zone) {
    return SCHEME_AND_HOST + "/z/" + zone.value();
  }

  private static String articleUrl(Article article) {
    return zoneUrl(article.getZone()) + "/debates/" + article.getArticleId().toString();
  }

  @Override
  protected void buildFeedMetadata(Map<String, Object> model,
      Channel feed,
      HttpServletRequest request) {
    ZoneInfo zoneInfo = (ZoneInfo) model.get("zoneInfo");
    if (zoneInfo != null) {
      feed.setTitle(zoneInfo.getName() + " kaif.io");
      feed.setLink(zoneUrl(zoneInfo.getZone()));
      feed.setDescription(zoneInfo.getAliasName() + " 熱門");
    } else {
      feed.setTitle("熱門 kaif.io");
      feed.setLink(SCHEME_AND_HOST);
      feed.setDescription("綜合熱門");
    }
    feed.setPubDate(Date.from(Instant.now()));
  }

  private Item convertArticle(Article article) {
    Item entry = new Item();
    Guid guid = new Guid();
    guid.setValue(article.getArticleId().toString());
    entry.setGuid(guid);
    entry.setTitle(article.getTitle());
    entry.setPubDate(Date.from(article.getCreateTime()));
    Description summary = new Description();
    summary.setType("html");
    summary.setValue(buildSummary(article));
    entry.setDescription(summary);
    entry.setLink(articleUrl(article));
    return entry;
  }

  private String buildAuthorPart(String username) {
    StringBuilder builder = new StringBuilder();
    builder.append("由");
    builder.append("<a href=\"");
    builder.append("https://kaif.io/u/");
    builder.append(username);
    builder.append("\">");
    builder.append(username);
    builder.append("</a>張貼<br>");
    return builder.toString();
  }

  private String buildSummary(Article article) {
    StringBuilder builder = new StringBuilder();
    if (article.isExternalLink()) {
      builder.append(buildAuthorPart(article.getAuthorName()));
      builder.append("<a href=\"");
      builder.append(article.getLink());
      builder.append("\">");
      builder.append("[原文連結]");
      builder.append("</a>");
    } else {
      builder.append("<div>");
      builder.append(article.getRenderContent());
      builder.append("</div>");
      builder.append(buildAuthorPart(article.getAuthorName()));
    }

    builder.append("<a href=\"");
    builder.append(articleUrl(article));
    builder.append("\">[");
    builder.append(article.getDebateCount());
    builder.append("個討論]");
    builder.append("</a><br>");
    return builder.toString();
  }

  @Override
  protected List<Item> buildFeedItems(Map<String, Object> model,
      HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    @SuppressWarnings("unchecked")
    List<Article> hotArticles = (List<Article>) model.get("articlePage");
    return hotArticles.stream().map(this::convertArticle).collect(Collectors.toList());
  }
}
