package io.kaif.model;

import io.kaif.flake.FlakeIdGenerator;

/**
 * in kaif, all time depends model share the same flakeId generator. so id are unique
 * amount all assets.
 * <p>
 * this is required for Article/Debate url shorten-er because both entities share same url pattern
 */
public class KaifIdGenerator extends FlakeIdGenerator {
  public KaifIdGenerator(int nodeId) {
    super(nodeId);
  }
}
