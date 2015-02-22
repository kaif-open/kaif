package io.kaif.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import freemarker.template.TemplateModelException;
import io.kaif.service.AccountService;
import io.kaif.web.AccountAccessTokenArgumentResolver;
import io.kaif.web.support.RelativeTime;

@Configuration
public class WebConfiguration extends WebMvcConfigurerAdapter {

  @Autowired
  private AccountService accountService;

  @Autowired
  private freemarker.template.Configuration freeMarkerConfiguration;

  @Autowired
  private Environment environment;

  @Autowired
  private AppProperties appProperties;

  @Autowired
  private ResourceBundleMessageSource messageSource;

  /**
   * add module Java 8 Time and AfterBurner in class path
   */
  @SuppressWarnings("unchecked")
  @Bean
  public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
    return new Jackson2ObjectMapperBuilder().modulesToInstall(AfterburnerModule.class);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    //spring-boot already include `/webjars/**` and `/**` for all static resources
    // and it also apply cache header if spring.resources.cache-period property exist.

    // so in production, following resources are cached:
    //
    //   /favicon.ico
    //   /robots.txt
    //   /webjars/**
    //   /build-2015..../**
    //   ...etc
    //

    // below are source code of WebMvcAutoConfiguration:
    //
    //      registry.addResourceHandler("/webjars/**")
    //          .addResourceLocations("classpath:/META-INF/resources/webjars/")
    //          .setCachePeriod(cachePeriod);
    //      registry.addResourceHandler("/**")
    //          .addResourceLocations(RESOURCE_LOCATIONS)
    //          .setCachePeriod(cachePeriod);

  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(new AccountAccessTokenArgumentResolver(accountService));
  }

  // change locale only enable in dev mode, see {@link #addInterceptors}
  @Profile(SpringProfile.DEV)
  @Bean
  public LocaleResolver localeResolver() {
    CookieLocaleResolver localeResolver = new CookieLocaleResolver();
    localeResolver.setCookieName("kaif-locale");
    localeResolver.setDefaultLocale(Locale.TAIWAN);
    return localeResolver;
  }

  @Profile(SpringProfile.DEV)
  @Bean
  public LocaleChangeInterceptor localeChangeInterceptor() {
    LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
    localeChangeInterceptor.setParamName("kaif-locale");
    return localeChangeInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    if (environment.acceptsProfiles(SpringProfile.DEV)) {
      //currently we only allow change locale in dev mode for debug
      registry.addInterceptor(localeChangeInterceptor());
    }
  }

  @PostConstruct
  public void init() throws TemplateModelException {
    configureFreeMarker();

    // default is true, which fallback to system locale, so if
    // system locale is jp. fallback bundle file will be messages_jp.properties, not
    // messages.properties. This behavior is not what we want. so disable it.
    messageSource.setFallbackToSystemLocale(false);
  }

  private void configureFreeMarker() throws TemplateModelException {
    Map<String, Object> variables = new HashMap<>();
    variables.put("appBuild", appProperties.getBuild());
    variables.put("profilesActive",
        Arrays.stream(environment.getActiveProfiles()).collect(Collectors.joining(",")));
    freeMarkerConfiguration.setSharedVariable("kaif", variables);
    freeMarkerConfiguration.setSharedVariable("relativeTime", new RelativeTime());
  }
}
