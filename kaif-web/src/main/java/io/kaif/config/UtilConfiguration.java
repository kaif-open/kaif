package io.kaif.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.common.cache.CacheBuilder;

@Configuration
@EnableCaching
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class UtilConfiguration {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @Primary
  public CacheManager compositeCacheManager() {
    return new CompositeCacheManager(debaterIdCacheManager(), zoneInfoCacheManager());
  }

  @Bean
  public CacheManager debaterIdCacheManager() {
    CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.DAYS)
        .maximumSize(2000);
    GuavaCacheManager cacheManager = new GuavaCacheManager("DebaterId");
    cacheManager.setCacheBuilder(cacheBuilder);
    return cacheManager;
  }

  @Bean
  public CacheManager zoneInfoCacheManager() {
    CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .maximumSize(Integer.MAX_VALUE);
    GuavaCacheManager cacheManager = new GuavaCacheManager("ZoneInfo");
    cacheManager.setCacheBuilder(cacheBuilder);
    return cacheManager;
  }

}

