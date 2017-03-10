package io.kaif;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * This is an helper Java class that provides an alternative to creating a web.xml.
 */
public class ApplicationWebXml extends SpringBootServletInitializer {

  private final Logger log = LoggerFactory.getLogger(ApplicationWebXml.class);

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(Application.class);
  }

}
