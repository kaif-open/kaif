package io.kaif.web.v1.dto;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "DebateNode", description = "Nested tree of debates")
public class V1DebateNodeDto {

  @ApiModelProperty(value = "debate of current node, null if root node", required = false)
  private final V1DebateDto debate;

  @ApiModelProperty(value = "children of debate node", required = true)
  private final List<V1DebateNodeDto> children;

  public V1DebateNodeDto(V1DebateDto debate, List<V1DebateNodeDto> children) {
    this.debate = debate;
    this.children = children;
  }

  @Override
  public String toString() {
    return "V1DebateNodeDto{" +
        "parent=" + debate +
        ", children=" + children +
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

    V1DebateNodeDto that = (V1DebateNodeDto) o;

    if (debate != null ? !debate.equals(that.debate) : that.debate != null) {
      return false;
    }
    return !(children != null ? !children.equals(that.children) : that.children != null);

  }

  @Override
  public int hashCode() {
    int result = debate != null ? debate.hashCode() : 0;
    result = 31 * result + (children != null ? children.hashCode() : 0);
    return result;
  }

  public V1DebateDto getDebate() {
    return debate;
  }

  public List<V1DebateNodeDto> getChildren() {
    return children;
  }
}
