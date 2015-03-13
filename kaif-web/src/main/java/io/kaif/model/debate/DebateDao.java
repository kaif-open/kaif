package io.kaif.model.debate;

import static io.kaif.util.MoreCollectors.toImmutableMap;
import static java.util.stream.Collectors.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import io.kaif.database.DaoOperations;
import io.kaif.flake.FlakeId;
import io.kaif.model.KaifIdGenerator;
import io.kaif.model.account.Account;
import io.kaif.model.article.Article;
import io.kaif.model.zone.Zone;

@Repository
public class DebateDao implements DaoOperations {

  private static final Logger logger = LoggerFactory.getLogger(DebateDao.class);
  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  @Autowired
  private KaifIdGenerator kaifIdGenerator;
  private final RowMapper<Debate> debateMapper = (rs, rowNum) -> {

    return new Debate(FlakeId.valueOf(rs.getLong("articleId")),
        FlakeId.valueOf(rs.getLong("debateId")),
        Zone.valueOf(rs.getString("zone")),
        FlakeId.valueOf(rs.getLong("parentDebateId")),
        UUID.fromString(rs.getString("replyToAccountId")),
        rs.getInt("level"),
        rs.getString("content"),
        DebateContentType.valueOf(rs.getString("contentType")),
        UUID.fromString(rs.getString("debaterId")),
        rs.getString("debaterName"),
        rs.getLong("upVote"),
        rs.getLong("downVote"),
        rs.getTimestamp("createTime").toInstant(),
        rs.getTimestamp("lastUpdateTime").toInstant());
  };

  /**
   * although cached debate will be evict when content updated, but the total vote is not real time
   * value. so the voting count will be 10 minutes delayed currently. and DebateTree is not yet
   * cache yet, so user may see inconsistent.
   * <p>
   * TODO we should use refreshAfterWrite() here but due to guava loadAll() issue
   * (https://github.com/google/guava/issues/1975)
   * we have to use expireAfterWrite() to allow more loadAll() optimization
   */
  private final LoadingCache<FlakeId, Debate> debatesCache = CacheBuilder.newBuilder()
      .maximumSize(2000)
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build(new CacheLoader<FlakeId, Debate>() {
        @Override
        public Debate load(FlakeId key) throws Exception {
          return loadDebate(key);
        }

        @Override
        public Map<FlakeId, Debate> loadAll(Iterable<? extends FlakeId> keys) throws Exception {
          return listDebatesByIdWithoutCache(Lists.newArrayList(keys));
        }
      });

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  private Debate insertDebate(Debate debate) {
    jdbc().update(""
            + " INSERT "
            + "   INTO Debate "
            + "        (articleid, debateid, zone, parentdebateid,replyToAccountId, level, "
            + "         content, contenttype, "
            + "         debaterid, debatername, upvote, downvote, createtime, lastupdatetime)"
            + " VALUES "
            + questions(14),
        debate.getArticleId().value(),
        debate.getDebateId().value(),
        debate.getZone().value(),
        debate.getParentDebateId().value(),
        debate.getReplyToAccountId(),
        debate.getLevel(),
        debate.getContent(),
        debate.getContentType().name(),
        debate.getDebaterId(),
        debate.getDebaterName(),
        debate.getUpVote(),
        debate.getDownVote(),
        Timestamp.from(debate.getCreateTime()),
        Timestamp.from(debate.getLastUpdateTime()));
    return debate;
  }

  public Optional<Debate> findDebate(FlakeId debateId) {
    if (Debate.NO_PARENT.equals(debateId)) {
      return Optional.empty();
    }
    final String sql = " SELECT * FROM Debate WHERE debateId = ? LIMIT 1 ";
    return jdbc().query(sql, debateMapper, debateId.value()).stream().findAny();
  }

  public Debate create(Article article,
      @Nullable Debate parent,
      String content,
      Account debater,
      Instant now) {
    //TODO evict DebateTree cache
    FlakeId debateId = kaifIdGenerator.next();
    return insertDebate(Debate.create(article, debateId, parent, content, debater, now));
  }

  public DebateTree listDebateTreeByArticle(FlakeId articleId, @Nullable FlakeId parentDebateId) {
    //TODO cache whole DebateTree
    List<Debate> flatten = listDepthFirstDebatesByArticle(articleId, parentDebateId);
    return DebateTree.fromDepthFirst(flatten).sortByBestScore();
  }

  List<Debate> listDepthFirstDebatesByArticle(FlakeId articleId, @Nullable FlakeId parentDebateId) {
    // TODO LIMIT in query, compute score in SQL
    // http://stackoverflow.com/a/25486998
    final String sql = ""
        + " WITH RECURSIVE DebateTree "
        + " AS "
        + " ( "
        + "    SELECT *, "
        + "           ARRAY[debateId] AS path "
        + "      FROM Debate "
        + "     WHERE articleId = :articleId "
        + "       AND parentDebateId = :parentDebateId "
        + "     UNION "
        + "    SELECT d.*,"
        + "           DebateTree.path || d.debateId AS path "
        + "      FROM DebateTree "
        + "      JOIN Debate d ON d.parentDebateId = DebateTree.debateId "
        + "     WHERE d.articleId = :articleId "
        + " ) "
        + " SELECT * FROM DebateTree ORDER BY path ";

    FlakeId parent = Optional.ofNullable(parentDebateId).orElse(Debate.NO_PARENT);
    Map<String, Object> params = ImmutableMap.of(//
        "articleId", articleId.value(), "parentDebateId", parent.value());

    return namedJdbc().query(sql, params, debateMapper);
  }

  public void changeTotalVote(FlakeId debateId, long upVoteDelta, long downVoteDelta) {
    if (upVoteDelta == 0 && downVoteDelta == 0) {
      return;
    }
    jdbc().update(""
        + " UPDATE Debate "
        + "    SET upVote = upVote + (?) "
        + "      , downVote = downVote + (?) "
        + "  WHERE debateId = ? ", upVoteDelta, downVoteDelta, debateId.value());
    //should we evict debatesCache ?
  }

  public UUID loadDebaterId(FlakeId debateId) throws EmptyResultDataAccessException {
    try {
      return debatesCache.get(debateId).getDebaterId();
    } catch (ExecutionException e) {
      return loadDebaterIdWithoutCache(debateId);
    }
  }

  private UUID loadDebaterIdWithoutCache(FlakeId debateId) throws EmptyResultDataAccessException {
    return UUID.fromString(jdbc().queryForObject(" SELECT debaterId FROM Debate WHERE debateId = ? ",
        String.class,
        debateId.value()));
  }

  public Debate loadDebate(FlakeId debateId) {
    return jdbc().queryForObject(" SELECT * FROM Debate WHERE debateId = ? ",
        debateMapper,
        debateId.value());
  }

  public void updateContent(FlakeId debateId, String content, Instant now) {
    jdbc().update(""
        + " UPDATE Debate "
        + "    SET content = ?"
        + "      , lastUpdateTime = ? "
        + "  WHERE debateId = ? ", content, Timestamp.from(now), debateId.value());
    debatesCache.invalidate(debateId);
    //TODO evict DebateTree
  }

  public List<Debate> listLatestDebateByReplyTo(UUID replyToAccountId,
      @Nullable FlakeId startDebateId,
      int size) {
    FlakeId start = Optional.ofNullable(startDebateId).orElse(FlakeId.MAX);
    ImmutableMap<String, Object> params = ImmutableMap.of("accountId",
        replyToAccountId,
        "start",
        start.value(),
        "size",
        size);
    return namedJdbc().query(""
        + " SELECT * "
        + "   FROM Debate "
        + "  WHERE replyToAccountId = :accountId "
        + "    AND debaterid <> :accountId "
        + "    AND debateId < :start "
        + "  ORDER BY debateid DESC "
        + "  LIMIT :size ", params, debateMapper);
  }

  public List<Debate> listDebatesByTimeDesc(FlakeId startDebateId, int size) {
    FlakeId start = Optional.ofNullable(startDebateId).orElse(FlakeId.MAX);
    return jdbc().query(""
        + " SELECT * "
        + "   FROM Debate "
        + "  WHERE debateId < ? "
        + "  ORDER BY debateId DESC "
        + "  LIMIT ? ", debateMapper, start.value(), size);
  }

  public List<Debate> listZoneDebatesByTimeDesc(Zone zone, FlakeId startDebateId, int size) {
    FlakeId start = Optional.ofNullable(startDebateId).orElse(FlakeId.MAX);
    return jdbc().query(""
        + " SELECT * "
        + "   FROM Debate "
        + "  WHERE debateId < ? "
        + "    AND zone = ? "
        + "  ORDER BY debateId DESC "
        + "  LIMIT ? ", debateMapper, start.value(), zone.value(), size);
  }

  public List<Debate> listDebatesById(List<FlakeId> debateIds) {
    if (debateIds.isEmpty()) {
      return Collections.emptyList();
    }
    Map<FlakeId, Debate> results;
    try {
      results = debatesCache.getAll(debateIds);
    } catch (ExecutionException e) {
      logger.warn("list debates by id cache failed", e);
      results = listDebatesByIdWithoutCache(debateIds);
    }
    return results.values().stream().distinct().collect(toList());
  }

  private Map<FlakeId, Debate> listDebatesByIdWithoutCache(List<FlakeId> debateIds) {
    if (debateIds.isEmpty()) {
      return Collections.emptyMap();
    }
    List<Debate> debates = namedJdbc().query(" SELECT * FROM Debate WHERE debateId IN (:ids) ",
        ImmutableMap.of("ids", debateIds.stream().map(FlakeId::value).collect(toList())),
        debateMapper);
    return debates.stream().collect(toImmutableMap(Debate::getDebateId, Function.identity()));
  }
}
