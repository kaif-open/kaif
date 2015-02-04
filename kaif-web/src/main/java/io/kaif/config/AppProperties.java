package io.kaif.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("app")
public class AppProperties {
  private String build;

  public String getBuild() {
    return build;
  }

  public void setBuild(String build) {
    this.build = build;
  }
}