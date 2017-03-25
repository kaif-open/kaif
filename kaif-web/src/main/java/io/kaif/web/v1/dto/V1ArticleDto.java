package io.kaif.web.v1.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.kaif.web.v1.V1Commons;

@ApiModel("Article")
public class V1ArticleDto {

  @ApiModelProperty(required = true)
  private final String zone;

  @ApiModelProperty(required = true)
  private final String zoneTitle;

  @ApiModelProperty(required = true)
  private final String articleId;

  @ApiModelProperty(required = true)
  private final String title;

  @ApiModelProperty(required = true, dataType = V1Commons.API_DATA_TYPE_DATE_TIME,
      value = "create time in ISO8601 format")
  @JsonFormat(pattern = V1Commons.JSON_ISO_DATE_PATTERN)
  private final Date createTime;

  @ApiModelProperty(value = "url of external link article, null if articleType is SPEAK", required = false)
  private final String link;

  @ApiModelProperty(value = "content of speak article, null if articleType is EXTERNAL_LINK", required = false)
  private final String content;

  @ApiModelProperty(required = true)
  private final V1ArticleType articleType;

  @ApiModelProperty(value = "username of article author", required = true)
  private final String authorName;

  @ApiModelProperty(value = "total up voted count", required = true)
  private final long upVote;

  @ApiModelProperty(value = "total debate count", required = true)
  private final long debateCount;

  @ApiModelProperty(required = true)
  private final boolean deleted;

  public V1ArticleDto(String zone,
      String zoneTitle,
      String articleId,
      String title,
      String link,
      String content,
      V1ArticleType articleType,
      Date createTime,
      String authorName,
      long upVote,
      long debateCount,
      boolean deleted) {
    this.zone = zone;
    this.zoneTitle = zoneTitle;
    this.articleId = articleId;
    this.title = title;
    this.link = link;
    this.content = content;
    this.articleType = articleType;
    this.createTime = createTime;
    this.authorName = authorName;
    this.upVote = upVote;
    this.debateCount = debateCount;
    this.deleted = deleted;
  }

  public String getZone() {
    return zone;
  }

  @Override
  public String toString() {
    return "V1ArticleDto{" +
        "zone='" + zone + '\'' +
        ", zoneTitle='" + zoneTitle + '\'' +
        ", articleId='" + articleId + '\'' +
        ", title='" + title + '\'' +
        ", createTime=" + createTime +
        ", link='" + link + '\'' +
        ", content='" + content + '\'' +
        ", articleType=" + articleType +
        ", authorName='" + authorName + '\'' +
        ", upVote=" + upVote +
        ", debateCount=" + debateCount +
        ", deleted=" + deleted +
        '}';
  }

  public boolean isDeleted() {
    return deleted;
  }

  public String getZoneTitle() {
    return zoneTitle;
  }

  public String getArticleId() {
    return articleId;
  }

  public String getTitle() {
    return title;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public String getLink() {
    return link;
  }

  public String getContent() {
    return content;
  }

  public V1ArticleType getArticleType() {
    return articleType;
  }

  public String getAuthorName() {
    return authorName;
  }

  public long getUpVote() {
    return upVote;
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

    V1ArticleDto that = (V1ArticleDto) o;

    if (upVote != that.upVote) {
      return false;
    }
    if (debateCount != that.debateCount) {
      return false;
    }
    if (deleted != that.deleted) {
      return false;
    }
    if (zone != null ? !zone.equals(that.zone) : that.zone != null) {
      return false;
    }
    if (zoneTitle != null ? !zoneTitle.equals(that.zoneTitle) : that.zoneTitle != null) {
      return false;
    }
    if (articleId != null ? !articleId.equals(that.articleId) : that.articleId != null) {
      return false;
    }
    if (title != null ? !title.equals(that.title) : that.title != null) {
      return false;
    }
    if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) {
      return false;
    }
    if (link != null ? !link.equals(that.link) : that.link != null) {
      return false;
    }
    if (content != null ? !content.equals(that.content) : that.content != null) {
      return false;
    }
    if (articleType != that.articleType) {
      return false;
    }
    return !(authorName != null ? !authorName.equals(that.authorName) : that.authorName != null);

  }

  @Override
  public int hashCode() {
    int result = zone != null ? zone.hashCode() : 0;
    result = 31 * result + (zoneTitle != null ? zoneTitle.hashCode() : 0);
    result = 31 * result + (articleId != null ? articleId.hashCode() : 0);
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
    result = 31 * result + (link != null ? link.hashCode() : 0);
    result = 31 * result + (content != null ? content.hashCode() : 0);
    result = 31 * result + (articleType != null ? articleType.hashCode() : 0);
    result = 31 * result + (authorName != null ? authorName.hashCode() : 0);
    result = 31 * result + (int) (upVote ^ (upVote >>> 32));
    result = 31 * result + (int) (debateCount ^ (debateCount >>> 32));
    result = 31 * result + (deleted ? 1 : 0);
    return result;
  }
}
