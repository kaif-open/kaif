package io.kaif.model.debate;

import io.kaif.flake.FlakeIdGenerator;

/**
 * Article and Debate share the same flakeId generator. so id are unique amount Article and Debate
 * <p>
 * this is required for url shorten-er because both entity share same url pattern
 */
public class DebateFlakeIdGenerator extends FlakeIdGenerator {
  public DebateFlakeIdGenerator(int nodeId) {
    super(nodeId);
  }
}
