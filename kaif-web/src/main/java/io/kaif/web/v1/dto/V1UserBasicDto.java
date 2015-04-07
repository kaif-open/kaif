package io.kaif.web.v1.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import io.kaif.web.v1.V1Commons;

@ApiModel("UserBasic")
public class V1UserBasicDto {

  @ApiModelProperty(required = true)
  private final String username;

  private final String description;

  @ApiModelProperty(required = true, dataType = V1Commons.API_DATA_TYPE_DATE_TIME,
      value = "registration time in ISO8601 format")
  @JsonFormat(pattern = V1Commons.JSON_ISO_DATE_PATTERN)
  private final Date createTime;

  public V1UserBasicDto(String username, String description, Date createTime) {
    this.username = username;
    this.description = description;
    this.createTime = createTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    V1UserBasicDto that = (V1UserBasicDto) o;

    if (username != null ? !username.equals(that.username) : that.username != null) {
      return false;
    }
    if (description != null ? !description.equals(that.description) : that.description != null) {
      return false;
    }
    return !(createTime != null ? !createTime.equals(that.createTime) : that.createTime != null);

  }

  @Override
  public int hashCode() {
    int result = username != null ? username.hashCode() : 0;
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "V1UserBasicDto{" +
        "username='" + username + '\'' +
        ", description='" + description + '\'' +
        ", createTime=" + createTime +
        '}';
  }

  public String getUsername() {
    return username;
  }

  public String getDescription() {
    return description;
  }

  public Date getCreateTime() {
    return createTime;
  }
}
