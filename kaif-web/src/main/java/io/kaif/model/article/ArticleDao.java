package io.kaif.model.article;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import io.kaif.database.DaoOperations;
import io.kaif.flake.FlakeId;
import io.kaif.model.KaifIdGenerator;
import io.kaif.model.account.Account;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneDao;
import io.kaif.model.zone.ZoneInfo;

@Repository
public class ArticleDao implements DaoOperations {

  private static final Logger logger = LoggerFactory.getLogger(ArticleDao.class);

  private final RowMapper<Article> articleMapper = (rs, rowNum) -> {
    return new Article(//
        Zone.valueOf(rs.getString("zone")),
        rs.getString("aliasName"),
        FlakeId.valueOf(rs.getLong("articleId")),
        rs.getString("title"),
        rs.getString("link"),
        rs.getString("content"),
        ArticleContentType.valueOf(rs.getString("contentType")),
        rs.getTimestamp("createTime").toInstant(),
        UUID.fromString(rs.getString("authorId")),
        rs.getString("authorName"),
        rs.getBoolean("deleted"),
        rs.getLong("upVote"),
        rs.getLong("downVote"),
        rs.getLong("debateCount"));
  };

  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  @Autowired
  private KaifIdGenerator kaifIdGenerator;
  /**
   * TODO change to refreshAfterWrite after guava issue solved
   * <p>
   * see {@link io.kaif.model.debate.DebateDao#debatesCache} for why we have to
   * use expireAfterWrite()
   */
  @Autowired
  private ZoneDao zoneDao;
  private final LoadingCache<FlakeId, Article> articleByDebatesCache = CacheBuilder.newBuilder()
      .maximumSize(2000)
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build(new CacheLoader<FlakeId, Article>() {
        @Override
        public Article load(FlakeId key) throws Exception {
          return listArticlesByDebatesWithoutCache(asList(key)).get(key);
        }

        @Override
        public Map<FlakeId, Article> loadAll(Iterable<? extends FlakeId> keys) throws Exception {
          return listArticlesByDebatesWithoutCache(Lists.newArrayList(keys));
        }
      });

  public RowMapper<Article> getArticleMapper() {
    return articleMapper;
  }

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  @VisibleForTesting
  Article insertArticle(Article article) {
    jdbc().update(""
            + " INSERT "
            + "   INTO Article "
            + "        (zone, aliasName, articleid, title, link, content, contentType, "
            + "         createTime, authorid, authorname, deleted, upvote, downvote, debatecount)"
            + " VALUES "
            + questions(14),
        article.getZone().value(),
        article.getAliasName(),
        article.getArticleId().value(),
        article.getTitle(),
        article.getLink(),
        article.getContent(),
        article.getContentType().name(),
        Timestamp.from(article.getCreateTime()),
        article.getAuthorId(),
        article.getAuthorName(),
        article.isDeleted(),
        article.getUpVote(),
        article.getDownVote(),
        article.getDebateCount());
    return article;
  }

  public Optional<Article> findArticleWithoutCache(FlakeId articleId) {
    final String sql = " SELECT * FROM Article WHERE articleId = ? LIMIT 1 ";
    return jdbc().query(sql, articleMapper, articleId.value()).stream().findAny();
  }

  public List<Article> listZoneArticlesDesc(Zone zone,
      @Nullable FlakeId startArticleId,
      int limit) {
    FlakeId start = Optional.ofNullable(startArticleId).orElse(FlakeId.MAX);
    final String sql = ""
        + " SELECT * "
        + "   FROM Article "
        + "  WHERE articleid < ? "
        + "    AND zone = ? "
        + "    AND deleted = FALSE "
        + "  ORDER BY articleId DESC "
        + "  LIMIT ? ";
    return jdbc().query(sql, articleMapper, start.value(), zone.value(), limit);
  }

  /**
   * this is global articles list, but we don't filter hideFromTop articles
   */
  public List<Article> listArticlesDesc(@Nullable FlakeId startArticleId, int limit) {
    FlakeId start = Optional.ofNullable(startArticleId).orElse(FlakeId.MAX);
    final String sql = ""
        + " SELECT * "
        + "   FROM Article "
        + "  WHERE articleId < ? "
        + "    AND deleted = FALSE "
        + "  ORDER BY articleId DESC "
        + "  LIMIT ? ";
    return jdbc().query(sql, articleMapper, start.value(), limit);
  }

  public Article createExternalLink(ZoneInfo zoneInfo,
      Account author,
      String title,
      String url,
      Instant now) {
    FlakeId flakeId = kaifIdGenerator.next();
    return insertArticle(Article.createExternalLink(zoneInfo.getZone(),
        zoneInfo.getAliasName(),
        flakeId,
        author,
        title,
        url,
        now));
  }

  /**
   * @throws EmptyResultDataAccessException
   *     if not found
   */
  public Article loadArticleWithoutCache(FlakeId articleId) throws EmptyResultDataAccessException {
    final String sql = " SELECT * FROM Article WHERE articleId = ? ";
    return jdbc().queryForObject(sql, articleMapper, articleId.value());
  }

  @Cacheable("Article")
  public Article loadArticleWithCache(FlakeId articleId) throws EmptyResultDataAccessException {
    return loadArticleWithoutCache(articleId);
  }

  public void increaseDebateCount(Article article) {
    jdbc().update(" UPDATE Article SET debateCount = debateCount + 1 WHERE articleId = ? ",
        article.getArticleId().value());
  }

  public void changeTotalVote(FlakeId articleId, long upVoteDelta, int downVoteDelta) {
    if (upVoteDelta == 0 && downVoteDelta == 0) {
      return;
    }
    jdbc().update(""
        + " UPDATE Article "
        + "    SET upVote = upVote + (?) "
        + "      , downVote = downVote + (?) "
        + "  WHERE articleId = ? ", upVoteDelta, downVoteDelta, articleId.value());
  }

  @VisibleForTesting
  double hotRanking(long upVoted, long downVoted, Instant createTime) {
    return jdbc().queryForObject(" SELECT hotRanking(?, ?, ?) ",
        Double.class,
        upVoted,
        downVoted,
        Timestamp.from(createTime));
  }

  public List<Article> listZoneHotArticles(Zone zone, @Nullable FlakeId startArticleId, int limit) {
    //TODO this is naive implementation, should improve performance later
    //possible improving is use startArticleId's max score as createTime hint
    if (startArticleId == null) {
      final String sql = ""
          + " SELECT * "
          + "   FROM Article "
          + "  WHERE zone = ? "
          + "    AND deleted = FALSE "
          + "  ORDER BY hotRanking(upVote, downVote, createTime) DESC "
          + "  LIMIT ? ";
      return jdbc().query(sql, articleMapper, zone.value(), limit);
    }
    final String sql = ""
        + " WITH RankArticle "
        + "   AS ( "
        + "       SELECT *, hotRanking(upVote, downVote, createTime) AS hot "
        + "         FROM Article "
        + "        WHERE zone = ? "
        + "      ) "
        + " SELECT * "
        + "   FROM RankArticle "
        + "  WHERE hot < ( SELECT hot FROM RankArticle WHERE articleId = ? ) "
        + "    AND deleted = FALSE "
        + "  ORDER BY hot DESC "
        + "  LIMIT ? ";
    return jdbc().query(sql, articleMapper, zone.value(), startArticleId.value(), limit);
  }

  public List<Article> listHotArticlesExcludeHidden(@Nullable FlakeId startArticleId, int limit) {
    //TODO this is naive implementation, should improve performance later

    //TODO test upper time bound
    Instant startTime = Optional.ofNullable(startArticleId)
        .map(FlakeId::epochMilli)
        .map(Instant::ofEpochMilli)
        .orElseGet(Instant::now);

    //query record up to 7 days ago, we can reduce days if articles grow after go production
    FlakeId upperTimeBound = FlakeId.startOf(startTime.minus(Duration.ofDays(7)).toEpochMilli());

    if (startArticleId == null) {
      final String sql = ""
          + " SELECT a.* "
          + "   FROM Article a "
          + "   JOIN ZoneInfo z ON a.zone = z.zone "
          + "  WHERE a.articleId > ? "
          + "    AND z.hideFromTop = FALSE "
          + "    AND a.deleted = FALSE "
          + "  ORDER BY hotRanking(a.upVote, a.downVote, a.createTime) DESC "
          + "  LIMIT ? ";
      return jdbc().query(sql, articleMapper, upperTimeBound.value(), limit);
    }
    final String sql = ""
        + " WITH RankArticle "
        + "   AS ( "
        + "       SELECT a.*, hotRanking(a.upVote, a.downVote, a.createTime) AS hot "
        + "         FROM Article a"
        + "         JOIN ZoneInfo z ON a.zone = z.zone "
        + "        WHERE a.articleId > ? "
        + "          AND z.hideFromTop = FALSE "
        + "      ) "
        + " SELECT * "
        + "   FROM RankArticle "
        + "  WHERE hot < ( SELECT hot FROM RankArticle WHERE articleId = ? ) "
        + "    AND deleted = FALSE "
        + "  ORDER BY hot DESC "
        + "  LIMIT ? ";
    return jdbc().query(sql, articleMapper, upperTimeBound.value(), startArticleId.value(), limit);
  }

  @CacheEvict(value = "Article", key = "#a0.articleId")
  public void markAsDeleted(Article article) {
    jdbc().update(" UPDATE Article SET deleted = TRUE WHERE articleId = ? ",
        article.getArticleId().value());
  }

  public Article createSpeak(ZoneInfo zoneInfo,
      Account author,
      String title,
      String content,
      Instant now) {
    return insertArticle(Article.createSpeak(zoneInfo.getZone(),
        zoneInfo.getAliasName(),
        kaifIdGenerator.next(),
        author,
        title,
        content,
        now));
  }

  @VisibleForTesting
  @CacheEvict(value = { "listHotZones", "Article" }, allEntries = true)
  public void evictAllCaches() {
    articleByDebatesCache.invalidateAll();
  }

  /**
   * list hot zones based on article count, ignore zone that hideFromTop.
   * <p>
   * note that the result are cached for same size (argument Instant is not part of cache key)
   */
  @Cacheable(value = "listHotZones", key = "#a0")
  public List<ZoneInfo> listHotZonesWithCache(int size, Instant articleSince) {
    FlakeId startArticleId = FlakeId.startOf(articleSince.toEpochMilli());
    List<ZoneInfo> results = jdbc().query(""
        + " SELECT z.*, count(z.zone) AS zoneHotness "
        + "   FROM Article a "
        + "   JOIN ZoneInfo z ON z.zone = a.zone "
        + "  WHERE a.articleId >= ? "
        + "    AND z.hideFromTop = FALSE "
        + "  GROUP BY z.zone "
        + "  ORDER BY zoneHotness DESC "
        + "  LIMIT ? ", zoneDao.getZoneInfoMapper(), startArticleId.value(), size);
    //immutable for cache
    return ImmutableList.copyOf(results);
  }

  public List<Article> listArticlesByDebatesWithCache(List<FlakeId> debateIds) {
    Map<FlakeId, Article> articles;
    try {
      articles = articleByDebatesCache.getAll(debateIds);
    } catch (ExecutionException e) {
      logger.warn("list article by debates cache failed", e);
      articles = listArticlesByDebatesWithoutCache(debateIds);
    }
    return articles.values().stream().distinct().collect(toList());
  }

  private Map<FlakeId, Article> listArticlesByDebatesWithoutCache(List<FlakeId> debateIds) {
    if (debateIds.isEmpty()) {
      return Collections.emptyMap();
    }
    final String sql = ""
        + " SELECT d.debateId, a.* "
        + "   FROM Article a "
        + "   JOIN Debate d ON (d.articleId = a.articleId) "
        + "  WHERE d.debateId IN (:debateIds) ";
    List<Long> values = debateIds.stream().map(FlakeId::value).collect(toList());
    HashMap<FlakeId, Article> articles = new HashMap<>();
    namedJdbc().query(sql, ImmutableMap.of("debateIds", values), rs -> {
      FlakeId debateId = FlakeId.valueOf(rs.getLong("debateId"));
      articles.put(debateId, articleMapper.mapRow(rs, 0));
    });
    return articles;
  }

  public List<Article> listArticlesByAuthor(UUID authorId,
      @Nullable FlakeId startArticleId,
      int size) {
    FlakeId start = Optional.ofNullable(startArticleId).orElse(FlakeId.MAX);
    final String sql = ""
        + " SELECT * "
        + "   FROM Article "
        + "  WHERE articleId < ? "
        + "    AND authorId = ? "
        + "    AND deleted = FALSE "
        + "  ORDER BY articleId DESC "
        + "  LIMIT ? ";
    return jdbc().query(sql, articleMapper, start.value(), authorId, size);
  }
}
