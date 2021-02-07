package io.kaif.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.kaif.model.KaifIdGenerator;

@Configuration
public class ModelConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(KaifIdGenerator.class);

  @Bean
  public KaifIdGenerator debateFlakeIdGenerator() {
    int nodeId = resolveNodeId();
    logger.info("Use nodeId - " + nodeId);
    return new KaifIdGenerator(nodeId);
  }

  private int resolveNodeId() {
    try {
      return Math.abs(InetAddress.getLocalHost().getHostAddress().hashCode()) % (1 << 10);
    } catch (UnknownHostException e) {
      throw new RuntimeException(e);
    }
  }
}
