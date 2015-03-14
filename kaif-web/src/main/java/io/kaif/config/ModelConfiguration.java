package io.kaif.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import io.kaif.model.KaifIdGenerator;

@Configuration
public class ModelConfiguration {

  @Component
  @ConfigurationProperties("flake")
  public static class FlakeIdProperties {
    private int nodeId;

    public int getNodeId() {
      return nodeId;
    }

    public void setNodeId(int nodeId) {
      this.nodeId = nodeId;
    }
  }

  @Bean
  public KaifIdGenerator debateFlakeIdGenerator(FlakeIdProperties properties) {
    return new KaifIdGenerator(properties.nodeId);
  }
}
