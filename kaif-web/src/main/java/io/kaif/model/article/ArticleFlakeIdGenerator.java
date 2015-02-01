package io.kaif.model.article;

import io.kaif.flake.FlakeIdGenerator;

public class ArticleFlakeIdGenerator extends FlakeIdGenerator {
  public ArticleFlakeIdGenerator(int nodeId) {
    super(nodeId);
  }
}
