package io.kaif.web.v1.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import io.kaif.web.v1.V1Commons;

@ApiModel("Debate")
public class V1DebateDto {

  @ApiModelProperty(required = true)
  private final String articleId;

  @ApiModelProperty(required = true)
  private final String debateId;

  @ApiModelProperty(required = true)
  private final String zone;

  @ApiModelProperty(value = "reply to debateId, null if reply to article", required = false)
  private final String parentDebateId;

  @ApiModelProperty(value = "depth level of debate tree, start from 1", required = true)
  private final int level;

  @ApiModelProperty(required = true)
  private final String content;

  @ApiModelProperty(value = "debater's username", required = true)
  private final String debaterName;

  @ApiModelProperty(value = "total up voted count", required = true)
  private final long upVote;

  @ApiModelProperty(value = "total down voted count", required = true)
  private final long downVote;

  @ApiModelProperty(required = true, dataType = V1Commons.API_DATA_TYPE_DATE_TIME,
      value = "create time in ISO8601 format")
  @JsonFormat(pattern = V1Commons.JSON_ISO_DATE_PATTERN)
  private final Date createTime;

  @ApiModelProperty(required = true, dataType = V1Commons.API_DATA_TYPE_DATE_TIME,
      value = "content last update time, same as create time if not updated (ISO8601 format)")
  @JsonFormat(pattern = V1Commons.JSON_ISO_DATE_PATTERN)
  private final Date lastUpdateTime;

  public V1DebateDto(String articleId,
      String debateId,
      String zone,
      String parentDebateId,
      int level,
      String content,
      String debaterName,
      long upVote,
      long downVote,
      Date createTime,
      Date lastUpdateTime) {
    this.zone = zone;
    this.articleId = articleId;
    this.debateId = debateId;
    this.parentDebateId = parentDebateId;
    this.level = level;
    this.content = content;
    this.debaterName = debaterName;
    this.upVote = upVote;
    this.downVote = downVote;
    this.createTime = createTime;
    this.lastUpdateTime = lastUpdateTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    V1DebateDto that = (V1DebateDto) o;

    if (level != that.level) {
      return false;
    }
    if (upVote != that.upVote) {
      return false;
    }
    if (downVote != that.downVote) {
      return false;
    }
    if (articleId != null ? !articleId.equals(that.articleId) : that.articleId != null) {
      return false;
    }
    if (debateId != null ? !debateId.equals(that.debateId) : that.debateId != null) {
      return false;
    }
    if (zone != null ? !zone.equals(that.zone) : that.zone != null) {
      return false;
    }
    if (parentDebateId != null
        ? !parentDebateId.equals(that.parentDebateId)
        : that.parentDebateId != null) {
      return false;
    }
    if (content != null ? !content.equals(that.content) : that.content != null) {
      return false;
    }
    if (debaterName != null ? !debaterName.equals(that.debaterName) : that.debaterName != null) {
      return false;
    }
    if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) {
      return false;
    }
    return !(lastUpdateTime != null
             ? !lastUpdateTime.equals(that.lastUpdateTime)
             : that.lastUpdateTime != null);

  }

  @Override
  public int hashCode() {
    int result = articleId != null ? articleId.hashCode() : 0;
    result = 31 * result + (debateId != null ? debateId.hashCode() : 0);
    result = 31 * result + (zone != null ? zone.hashCode() : 0);
    result = 31 * result + (parentDebateId != null ? parentDebateId.hashCode() : 0);
    result = 31 * result + level;
    result = 31 * result + (content != null ? content.hashCode() : 0);
    result = 31 * result + (debaterName != null ? debaterName.hashCode() : 0);
    result = 31 * result + (int) (upVote ^ (upVote >>> 32));
    result = 31 * result + (int) (downVote ^ (downVote >>> 32));
    result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
    result = 31 * result + (lastUpdateTime != null ? lastUpdateTime.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "V1DebateDto{" +
        "articleId='" + articleId + '\'' +
        ", debateId='" + debateId + '\'' +
        ", zone='" + zone + '\'' +
        ", parentDebateId='" + parentDebateId + '\'' +
        ", level=" + level +
        ", content='" + content + '\'' +
        ", debaterName='" + debaterName + '\'' +
        ", upVote=" + upVote +
        ", downVote=" + downVote +
        ", createTime=" + createTime +
        ", lastUpdateTime=" + lastUpdateTime +
        '}';
  }

  public String getArticleId() {
    return articleId;
  }

  public String getDebateId() {
    return debateId;
  }

  public String getZone() {
    return zone;
  }

  public String getParentDebateId() {
    return parentDebateId;
  }

  public int getLevel() {
    return level;
  }

  public String getContent() {
    return content;
  }

  public String getDebaterName() {
    return debaterName;
  }

  public long getUpVote() {
    return upVote;
  }

  public long getDownVote() {
    return downVote;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public Date getLastUpdateTime() {
    return lastUpdateTime;
  }
}