package io.kaif.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.google.common.collect.ImmutableMap;

import freemarker.template.SimpleHash;
import freemarker.template.TemplateModelException;
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

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/");
  }

  @Autowired
  private AccountSecret accountSecret;

  @Autowired
  private AccountService accountService;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(new AccountAccessTokenArgumentResolver(accountSecret, accountService));
  }

  @Autowired
  private freemarker.template.Configuration freeMarkerConfiguration;

  @Autowired
  private Environment environment;

  @PostConstruct
  public void init() throws TemplateModelException {
    configureFreeMarker();
  }

  private void configureFreeMarker() throws TemplateModelException {
    Map<String, Object> variables = new HashMap<>();
    variables.put("deployServerTime", System.currentTimeMillis());
    variables.put("profilesActive",
        Arrays.stream(environment.getActiveProfiles()).collect(Collectors.joining(",")));
    freeMarkerConfiguration.setAllSharedVariables(new SimpleHash(ImmutableMap.of("kaif", variables),
        freeMarkerConfiguration.getObjectWrapper()));
  }
}
