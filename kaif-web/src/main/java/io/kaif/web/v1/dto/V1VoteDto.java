package io.kaif.web.v1.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import io.kaif.model.vote.VoteState;
import io.kaif.web.v1.V1Commons;

@ApiModel("Vote")
public class V1VoteDto {

  @ApiModelProperty(value = "depend on vote target, the id may be articleId or debateId", required = true)
  private final String targetId;

  @ApiModelProperty(value = "vote result", required = true)
  private final VoteState voteState;

  @ApiModelProperty(required = true, dataType = V1Commons.API_DATA_TYPE_DATE_TIME,
      value = "vote time in ISO8601 format")
  @JsonFormat(pattern = V1Commons.JSON_ISO_DATE_PATTERN)
  private final Date updateTime;

  public V1VoteDto(String targetId, VoteState voteState, Date updateTime) {
    this.targetId = targetId;
    this.voteState = voteState;
    this.updateTime = updateTime;
  }

  public String getTargetId() {
    return targetId;
  }

  public VoteState getVoteState() {
    return voteState;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    V1VoteDto v1VoteDto = (V1VoteDto) o;

    if (targetId != null ? !targetId.equals(v1VoteDto.targetId) : v1VoteDto.targetId != null) {
      return false;
    }
    if (voteState != v1VoteDto.voteState) {
      return false;
    }
    return !(updateTime != null
             ? !updateTime.equals(v1VoteDto.updateTime)
             : v1VoteDto.updateTime != null);

  }

  @Override
  public int hashCode() {
    int result = targetId != null ? targetId.hashCode() : 0;
    result = 31 * result + (voteState != null ? voteState.hashCode() : 0);
    result = 31 * result + (updateTime != null ? updateTime.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "V1VoteDto{" +
        "targetId='" + targetId + '\'' +
        ", voteState=" + voteState +
        ", updateTime=" + updateTime +
        '}';
  }
}
