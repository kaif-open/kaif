package io.kaif.model.article;

import java.time.Instant;
import java.util.UUID;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.zone.Zone;

public class Article {

  public static final int TITLE_MIN = 3;
  public static final int TITLE_MAX = 128;
  public static final int URL_MAX = 512;

  public static Article createExternalLink(Zone zone,
      FlakeId articleId,
      Account author,
      String title,
      String url,
      Instant now) {
    return new Article(zone,
        articleId,
        title,
        null,
        ArticleLinkType.EXTERNAL,
        now,
        url,
        ArticleContentType.URL,
        author.getAccountId(),
        author.getUsername(),
        false,
        0,
        0,
        0);
  }

  private final Zone zone;
  private final FlakeId articleId;
  private final String title;
  private final String urlName;
  private final ArticleLinkType linkType;
  private final Instant createTime;
  private final String content;
  private final ArticleContentType contentType;
  private final UUID authorId;
  private final String authorName;
  private final boolean deleted;
  private final long upVote;
  private final long downVote;
  private final long debateCount;

  Article(Zone zone,
      FlakeId articleId,
      String title,
      String urlName,
      ArticleLinkType linkType,
      Instant createTime,
      String content,
      ArticleContentType contentType,
      UUID authorId,
      String authorName,
      boolean deleted,
      long upVote,
      long downVote,
      long debateCount) {
    this.zone = zone;
    this.articleId = articleId;
    this.title = title;
    this.urlName = urlName;
    this.linkType = linkType;
    this.createTime = createTime;
    this.content = content;
    this.contentType = contentType;
    this.authorId = authorId;
    this.authorName = authorName;
    this.deleted = deleted;
    this.upVote = upVote;
    this.downVote = downVote;
    this.debateCount = debateCount;
  }

  public Zone getZone() {
    return zone;
  }

  public FlakeId getArticleId() {
    return articleId;
  }

  public String getTitle() {
    return title;
  }

  public String getUrlName() {
    return urlName;
  }

  public ArticleLinkType getLinkType() {
    return linkType;
  }

  public Instant getCreateTime() {
    return createTime;
  }

  public String getContent() {
    return content;
  }

  public ArticleContentType getContentType() {
    return contentType;
  }

  public UUID getAuthorId() {
    return authorId;
  }

  public String getAuthorName() {
    return authorName;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public long getUpVote() {
    return upVote;
  }

  public long getDownVote() {
    return downVote;
  }

  public long getDebateCount() {
    return debateCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Article article = (Article) o;

    if (!articleId.equals(article.articleId)) {
      return false;
    }
    if (!zone.equals(article.zone)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = zone.hashCode();
    result = 31 * result + articleId.hashCode();
    return result;
  }
}
