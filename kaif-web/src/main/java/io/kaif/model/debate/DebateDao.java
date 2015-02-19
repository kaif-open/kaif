package io.kaif.model.debate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.ImmutableMap;

import io.kaif.database.DaoOperations;
import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.article.Article;
import io.kaif.model.zone.Zone;

@Repository
public class DebateDao implements DaoOperations {

  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Autowired
  private DebateFlakeIdGenerator debateFlakeIdGenerator;

  private final RowMapper<Debate> debateMapper = (rs, rowNum) -> {

    return new Debate(FlakeId.valueOf(rs.getLong("articleId")),
        FlakeId.valueOf(rs.getLong("debateId")),
        Zone.valueOf(rs.getString("zone")),
        FlakeId.valueOf(rs.getLong("parentDebateId")),
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

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  private Debate insertDebate(Debate debate) {
    jdbc().update(""
            + " INSERT "
            + "   INTO Debate "
            + "        (articleid, debateid, zone, parentdebateid, level, content, contenttype, "
            + "         debaterid, debatername, upvote, downvote, createtime, lastupdatetime)"
            + " VALUES "
            + questions(13),
        debate.getArticleId().value(),
        debate.getDebateId().value(),
        debate.getZone().value(),
        debate.getParentDebateId().value(),
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

  //TODO evict cache
  public Debate create(Article article,
      @Nullable Debate parent,
      String content,
      Account debater,
      Instant now) {
    FlakeId debateId = debateFlakeIdGenerator.next();
    return insertDebate(Debate.create(article, debateId, parent, content, debater, now));
  }

  public DebateTree listDebateTreeByArticle(FlakeId articleId, @Nullable FlakeId parentDebateId) {
    //TODO cache
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
  }

  /**
   * @see io.kaif.config.UtilConfiguration#debaterIdCacheManager()
   */
  @Cacheable(value = "DebaterId")
  public UUID loadDebaterId(FlakeId debateId) throws EmptyResultDataAccessException {
    return UUID.fromString(jdbc().queryForObject(" SELECT debaterId FROM Debate WHERE debateId = ? ",
        String.class,
        debateId.value()));
  }

  public Debate loadDebate(FlakeId debateId) {
    return jdbc().queryForObject(" SELECT * FROM Debate WHERE debateId = ? ",
        debateMapper,
        debateId.value());
  }

  //TODO evict cache
  public void updateContent(FlakeId debateId, String content, Instant now) {
    jdbc().update(""
        + " UPDATE Debate "
        + "    SET content = ?"
        + "      , lastUpdateTime = ? "
        + "  WHERE debateId = ? ", content, Timestamp.from(now), debateId.value());
  }

}
