package io.kaif;

import java.util.Arrays;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application {

  private static final Logger log = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    ApplicationContext ctx = SpringApplication.run(Application.class, args);
    log.info("Application ready, active profiles:{}",
        Arrays.toString(ctx.getEnvironment().getActiveProfiles()));
  }

}