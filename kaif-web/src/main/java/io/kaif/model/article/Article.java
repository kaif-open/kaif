package io.kaif.model.article;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.time.Instant;
import java.util.UUID;

import javax.validation.UnexpectedTypeException;

import org.springframework.web.util.HtmlUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.kaif.flake.FlakeId;
import io.kaif.kmark.KmarkProcessor;
import io.kaif.model.account.Account;
import io.kaif.model.zone.Zone;
import io.kaif.web.v1.dto.ArticleType;
import io.kaif.web.v1.dto.V1ArticleDto;

public class Article {

  // note that checking string length should use unescaped one
  // database are allow more room for escaped string
  public static final int TITLE_MIN = 3;
  public static final int TITLE_MAX = 128;
  public static final int URL_MAX = 512;
  public static final int CONTENT_MIN = 10;
  public static final int CONTENT_MAX = 4096;

  //p{L} is unicode letter
  public static final String URL_PATTERN = "^(https?|ftp)://[\\p{L}\\w\\-]+\\.[\\p{L}\\w\\-]+.*";

  public static Article createSpeak(Zone zone,
      String zoneAliasName,
      FlakeId articleId,
      Account author,
      String title,
      String content,
      Instant now) {
    Preconditions.checkArgument(isValidTitle(title));
    Preconditions.checkArgument(isValidContent(content));
    String safeTitle = HtmlUtils.htmlEscape(title);
    return new Article(zone,
        zoneAliasName,
        articleId,
        safeTitle,
        null,
        content,
        ArticleContentType.MARK_DOWN,
        now,
        author.getAccountId(),
        author.getUsername(),
        false,
        0,
        0,
        0);
  }

  private static boolean isValidContent(String content) {
    return content != null && content.length() <= CONTENT_MAX && content.length() >= CONTENT_MIN;
  }

  public static Article createExternalLink(Zone zone,
      String zoneAliasName,
      FlakeId articleId,
      Account author,
      String title,
      String link,
      Instant now) {
    Preconditions.checkArgument(isValidTitle(title));
    Preconditions.checkArgument(isValidLink(link));
    String safeTitle = HtmlUtils.htmlEscape(title);
    String safeLink = HtmlUtils.htmlEscape(link);
    return new Article(zone,
        zoneAliasName,
        articleId,
        safeTitle,
        safeLink,
        null,
        ArticleContentType.NONE,
        now,
        author.getAccountId(),
        author.getUsername(),
        false,
        0,
        0,
        0);
  }

  private static boolean isValidLink(String link) {
    return link != null && link.length() <= URL_MAX && validateUrl(link);
  }

  private static boolean validateUrl(String url) {
    try {
      new URL(url);
      return true;
    } catch (MalformedURLException e) {
      return false;
    }
  }

  private static boolean isValidTitle(String title) {
    return title != null && title.length() <= TITLE_MAX && title.length() >= TITLE_MIN;
  }

  public static String renderSpeakPreview(String content) {
    return KmarkProcessor.process(content);
  }

  private final Zone zone;
  private final String aliasName;
  private final FlakeId articleId;
  private final String title;
  private final Instant createTime;
  private final String link;
  private final String content;
  private final ArticleContentType contentType;
  private final UUID authorId;
  private final String authorName;
  private final boolean deleted;
  private final long upVote;
  //article downVote count is preserved, not used
  private final long downVote;
  private final long debateCount;

  Article(Zone zone,
      String aliasName,
      FlakeId articleId,
      String title,
      String link,
      String content,
      ArticleContentType contentType,
      Instant createTime,
      UUID authorId,
      String authorName,
      boolean deleted,
      long upVote,
      long downVote,
      long debateCount) {
    this.zone = zone;
    this.aliasName = aliasName;
    this.articleId = articleId;
    this.title = title;
    this.link = link;
    this.content = content;
    this.contentType = contentType;
    this.createTime = createTime;
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

  public String getLink() {
    return link;
  }

  public String getAliasName() {
    return aliasName;
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

    return true;
  }

  @Override
  public int hashCode() {
    return articleId.hashCode();
  }

  @Override
  public String toString() {
    return "article:" +
        "/z/" + zone +
        "/article/" + articleId +
        "/" + title;
  }

  public String getLinkHint() {
    if (isExternalLink()) {
      try {
        return new URL(link).getHost();
      } catch (MalformedURLException e) {
        //this should never happened because constructor protected with design contract.
        throw new UnexpectedTypeException("malformed url");
      }
    } else {
      return "/z/" + zone;
    }
  }

  public V1ArticleDto toV1Dto() {
    return new V1ArticleDto(zone.value(),
        aliasName,
        articleId.toString(),
        title,
        link,
        content,
        isExternalLink() ? ArticleType.EXTERNAL_LINK : ArticleType.SPEAK,
        Date.from(createTime),
        authorName,
        upVote,
        debateCount);
  }

  /**
   * the method only allowed for article with content
   */
  public String getRenderContent() {
    switch (contentType) {
      case NONE:
        return "";
      case MARK_DOWN:
        return KmarkProcessor.process(content);
      case MATOME:
    }
    throw new UnsupportedOperationException("could not render with type:" + contentType);
  }

  public boolean isExternalLink() {
    return !Strings.isNullOrEmpty(link);
  }

  public boolean hasMarkDownContent() {
    return contentType == ArticleContentType.MARK_DOWN;
  }

  public String getShortUrlPath() {
    return String.format("/d/%s", getArticleId());
  }
}
