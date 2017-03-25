package io.kaif.web.v1.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("Zone")
public class V1ZoneDto {

  @ApiModelProperty(required = true)
  private final String name;

  @ApiModelProperty(value = "short description of zone, mostly in Chinese", required = true)
  private final String title;

  public V1ZoneDto(String name, String title) {
    this.name = name;
    this.title = title;
  }

  public String getName() {
    return name;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public String toString() {
    return "V1ZoneDto{" +
        "name='" + name + '\'' +
        ", title='" + title + '\'' +
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

    V1ZoneDto v1ZoneDto = (V1ZoneDto) o;

    if (name != null ? !name.equals(v1ZoneDto.name) : v1ZoneDto.name != null) {
      return false;
    }
    return !(title != null ? !title.equals(v1ZoneDto.title) : v1ZoneDto.title != null);

  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (title != null ? title.hashCode() : 0);
    return result;
  }
}
