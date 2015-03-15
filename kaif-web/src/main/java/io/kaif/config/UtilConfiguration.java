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
    return new CompositeCacheManager(zoneInfoCacheManager(),
        listHotZonesCacheManager(),
        rssHotArticlesCacheManager(),
        honorRollsCacheManager());
  }

  @Bean
  public CacheManager rssHotArticlesCacheManager() {
    CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
        .expireAfterWrite(4, TimeUnit.HOURS)
        .maximumSize(100);
    GuavaCacheManager cacheManager = new GuavaCacheManager("rssHotArticles");
    cacheManager.setCacheBuilder(cacheBuilder);
    return cacheManager;
  }

  @Bean
  public CacheManager zoneInfoCacheManager() {
    CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .maximumSize(2000);
    GuavaCacheManager cacheManager = new GuavaCacheManager("ZoneInfo");
    cacheManager.setCacheBuilder(cacheBuilder);
    return cacheManager;
  }

  /**
   * hot zones cache, refresh every one hour. no need to distribute if we have multiple web servers
   *
   * @see {@link io.kaif.model.article.ArticleDao#listHotZonesWithCache(int, java.time.Instant)}
   */
  @Bean
  public CacheManager listHotZonesCacheManager() {
    CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .maximumSize(100);
    GuavaCacheManager cacheManager = new GuavaCacheManager("listHotZones");
    cacheManager.setCacheBuilder(cacheBuilder);
    return cacheManager;
  }

  @Bean
  public CacheManager honorRollsCacheManager() {
    CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .maximumSize(100);
    GuavaCacheManager cacheManager = new GuavaCacheManager("listHonorRoll");
    cacheManager.setCacheBuilder(cacheBuilder);
    return cacheManager;
  }
}

