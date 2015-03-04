package io.kaif.model.debate;

import io.kaif.flake.FlakeIdGenerator;

/**
 * Article and Debate share the same flakeId generator. so id are unique amount Article and Debate
 */
public class DebateFlakeIdGenerator extends FlakeIdGenerator {
  public DebateFlakeIdGenerator(int nodeId) {
    super(nodeId);
  }
}
