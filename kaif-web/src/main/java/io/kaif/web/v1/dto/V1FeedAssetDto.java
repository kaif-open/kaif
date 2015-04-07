package io.kaif.web.v1.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import io.kaif.web.v1.V1Commons;

@ApiModel("FeedAsset")
public class V1FeedAssetDto {

  @ApiModelProperty(value = "id depends on assetType, if asset is debate the value is debateId", required = true)
  private final String assetId;

  @ApiModelProperty(required = true)
  private final V1AssetType assetType;

  @ApiModelProperty(required = true, dataType = V1Commons.API_DATA_TYPE_DATE_TIME,
      value = "create time in ISO8601 format")
  @JsonFormat(pattern = V1Commons.JSON_ISO_DATE_PATTERN)
  private final Date createTime;

  @ApiModelProperty(value = "the asset has been acknowledged", required = true)
  private final boolean acknowledged;

  @ApiModelProperty(value = "non null if assetType is debate related. otherwise is null", required = false)
  private final V1DebateDto debate;

  public V1FeedAssetDto(String assetId,
      V1AssetType assetType,
      Date createTime,
      boolean acknowledged,
      V1DebateDto debate) {
    this.assetId = assetId;
    this.assetType = assetType;
    this.createTime = createTime;
    this.acknowledged = acknowledged;
    this.debate = debate;
  }

  @Override
  public String toString() {
    return "V1FeedAssetDto{" +
        "assetId='" + assetId + '\'' +
        ", assetType=" + assetType +
        ", createTime=" + createTime +
        ", acknowledged=" + acknowledged +
        ", debate=" + debate +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    V1FeedAssetDto that = (V1FeedAssetDto) o;

    if (acknowledged != that.acknowledged) {
      return false;
    }
    if (assetId != null ? !assetId.equals(that.assetId) : that.assetId != null) {
      return false;
    }
    if (assetType != that.assetType) {
      return false;
    }
    if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) {
      return false;
    }
    return !(debate != null ? !debate.equals(that.debate) : that.debate != null);

  }

  @Override
  public int hashCode() {
    int result = assetId != null ? assetId.hashCode() : 0;
    result = 31 * result + (assetType != null ? assetType.hashCode() : 0);
    result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
    result = 31 * result + (acknowledged ? 1 : 0);
    result = 31 * result + (debate != null ? debate.hashCode() : 0);
    return result;
  }

  public V1DebateDto getDebate() {
    return debate;
  }

  public String getAssetId() {
    return assetId;
  }

  public V1AssetType getAssetType() {
    return assetType;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public boolean isAcknowledged() {
    return acknowledged;
  }
}
