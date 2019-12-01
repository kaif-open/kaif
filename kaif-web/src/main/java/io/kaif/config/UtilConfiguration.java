package io.kaif.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.github.benmanes.caffeine.cache.Caffeine;

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
        honorRollsCacheManager(),
        articleCacheManager(),
        findClientAppUserCacheManager(),
        listAdministratorsCacheManager());
  }

  @Bean
  public CacheManager rssHotArticlesCacheManager() {
    Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
        .expireAfterWrite(4, TimeUnit.HOURS)
        .maximumSize(100);
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("rssHotArticles");
    cacheManager.setCaffeine(cacheBuilder);
    return cacheManager;
  }

  @Bean
  public CacheManager zoneInfoCacheManager() {
    Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .maximumSize(2000);
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("ZoneInfo");
    cacheManager.setCaffeine(cacheBuilder);
    return cacheManager;
  }

  @Bean
  public CacheManager articleCacheManager() {
    Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .maximumSize(2000);
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("Article");
    cacheManager.setCaffeine(cacheBuilder);
    return cacheManager;
  }

  /**
   * hot zones cache, refresh every one hour. no need to distribute if we have multiple web servers
   *
   * @see {@link io.kaif.model.article.ArticleDao#listHotZonesWithCache(int, java.time.Instant)}
   */
  @Bean
  public CacheManager listHotZonesCacheManager() {
    Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .maximumSize(1000);
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("listHotZones");
    cacheManager.setCaffeine(cacheBuilder);
    return cacheManager;
  }

  /**
   * administrators cache, refresh every one minutes. no need to distribute if we have multiple
   * web servers (user just not see new administrators)
   */
  @Bean
  public CacheManager listAdministratorsCacheManager() {
    Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .maximumSize(1000);
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("listAdministrators");
    cacheManager.setCaffeine(cacheBuilder);
    return cacheManager;
  }

  /**
   * short life client app user cache (the cache is Optional<ClientAppUser>), so this is not
   * distribute-able
   *
   * @see {@link io.kaif.model.clientapp.ClientAppDao#findClientAppUserWithCache}
   */
  @Bean
  public CacheManager findClientAppUserCacheManager() {
    Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .maximumSize(1000);
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("findClientAppUser");
    cacheManager.setCaffeine(cacheBuilder);
    return cacheManager;
  }

  @Bean
  public CacheManager honorRollsCacheManager() {
    Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .maximumSize(100);
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("listHonorRoll");
    cacheManager.setCaffeine(cacheBuilder);
    return cacheManager;
  }
}

