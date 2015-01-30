package io.kaif.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
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

  /**
   * zoneInfo is not change frequently, so even we have multiple web servers, it is allowed cached
   * locally. one minute later the update will be showed in all servers
   *
   * @see io.kaif.model.zone.ZoneDao
   */
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
