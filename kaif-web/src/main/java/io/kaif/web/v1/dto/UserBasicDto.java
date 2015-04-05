package io.kaif.web.v1.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import io.kaif.web.v1.V1Commons;

@ApiModel("UserBasic")
public class UserBasicDto {

  @ApiModelProperty(required = true)
  private final String username;

  private final String description;

  @ApiModelProperty(required = true, dataType = V1Commons.API_DATA_TYPE_DATE_TIME,
      value = "registration time in ISO8601 format")
  @JsonFormat(pattern = V1Commons.JSON_ISO_DATE_PATTERN)
  private final Date createTime;

  public UserBasicDto(String username, String description, Date createTime) {
    this.username = username;
    this.description = description;
    this.createTime = createTime;
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
