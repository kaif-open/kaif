package io.kaif.model.article;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import io.kaif.database.DaoOperations;
import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneDao;
import io.kaif.model.zone.ZoneInfo;

@Repository
public class ArticleDao implements DaoOperations {

  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Autowired
  private ArticleFlakeIdGenerator articleFlakeIdGenerator;

  private final RowMapper<Article> articleMapper = (rs, rowNum) -> {
    return new Article(//
        Zone.valueOf(rs.getString("zone")),
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
  private ZoneDao zoneDao;

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  @VisibleForTesting
  Article insertArticle(Article article) {
    jdbc().update(""
            + " INSERT "
            + "   INTO Article "
            + "        (zone, articleid, title, link, content, contentType, "
            + "         createTime, authorid, authorname, deleted, upvote, downvote, debatecount)"
            + " VALUES "
            + questions(13),
        article.getZone().value(),
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

  public Optional<Article> findArticle(FlakeId articleId) {
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

  public Article createExternalLink(Zone zone,
      Account author,
      String title,
      String url,
      Instant now) {
    FlakeId flakeId = articleFlakeIdGenerator.next();
    return insertArticle(Article.createExternalLink(zone, flakeId, author, title, url, now));
  }

  /**
   * @throws EmptyResultDataAccessException
   *     if not found
   */
  public Article loadArticle(FlakeId articleId) throws EmptyResultDataAccessException {
    final String sql = " SELECT * FROM Article WHERE articleId = ? ";
    return jdbc().queryForObject(sql, articleMapper, articleId.value());
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

  //TODO evict any related cache
  public void markAsDeleted(Article article) {
    jdbc().update(" UPDATE Article SET deleted = TRUE WHERE articleId = ? ",
        article.getArticleId().value());
  }

  public Article createSpeak(Zone zone, Account author, String title, String content, Instant now) {
    return insertArticle(Article.createSpeak(zone,
        articleFlakeIdGenerator.next(),
        author,
        title,
        content,
        now));
  }

  @VisibleForTesting
  @CacheEvict(value = "listHotZones", allEntries = true)
  void evictAllCaches() {
  }

  /**
   * list hot zones based on article count, ignore zone that hideFromTop.
   * <p>
   * note that the result are cached for same size (argument Instant is not part of cache key)
   */
  @Cacheable(value = "listHotZones", key = "#a0")
  public List<ZoneInfo> listHotZones(int size, Instant articleSince) {
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
}
