package io.kaif.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import io.kaif.model.AccountService;
import io.kaif.model.account.AccountSecret;
import io.kaif.web.AccountAccessTokenArgumentResolver;

@Configuration
public class WebConfiguration extends WebMvcConfigurerAdapter {

  /**
   * add module Java 8 Time and AfterBurner in class path
   */
  @Bean
  public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
    //noinspection unchecked
    return new Jackson2ObjectMapperBuilder().modulesToInstall(AfterburnerModule.class);
  }

  @Autowired
  private AccountSecret accountSecret;

  @Autowired
  private AccountService accountService;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(new AccountAccessTokenArgumentResolver(accountSecret, accountService));
  }
}
