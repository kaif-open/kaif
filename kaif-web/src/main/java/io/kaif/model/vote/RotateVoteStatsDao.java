package io.kaif.model.vote;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.ImmutableMap;

import io.kaif.database.DaoOperations;
import io.kaif.model.article.Article;
import io.kaif.model.zone.Zone;

@Repository
public class RotateVoteStatsDao implements DaoOperations {
  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private RowMapper<RotateVoteStats> rotateVoteStatsMapper = (rs, rowNum) -> {
    return new RotateVoteStats(UUID.fromString(rs.getString("accountId")),
        Zone.valueOf(rs.getString("zone")),
        rs.getString("bucket"),
        rs.getString("username"),
        rs.getLong("debateCount"),
        rs.getLong("articleCount"),
        rs.getLong("articleUpVoted"),
        rs.getLong("debateUpVoted"),
        rs.getLong("debateDownVoted")
    );
  };

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  public Optional<RotateVoteStats> findRotateVoteStats(UUID accountId, Zone zone, String bucket) {
    final String sql = " SELECT * FROM RotateVoteStats WHERE accountId = ? AND zone = ? AND bucket = ? LIMIT 1 ";
    return jdbc().query(sql, rotateVoteStatsMapper, accountId, zone.value(), bucket)
        .stream()
        .findAny();
  }

  public void updateRotateVoteStats(UUID accountId,
      Zone zone,
      String bucket,
      String username,
      int debateCount,
      int articleCount,
      int articleUpVoted,
      int debateUpVoted,
      int debateDownVoted) {
    String upsert = ""
        + "   WITH UpsertStats "
        + "     AS ("
        + "             UPDATE RotateVoteStats "
        + "                SET debateCount = debateCount + :deltaDebateCount "
        + "                  , articleCount = articleCount + :deltaArticleCount "
        + "                  , articleUpVoted = articleUpVoted + :deltaArticleUpVoted "
        + "                  , debateUpVoted = debateUpVoted+ :deltaDebateUpVoted "
        + "                  , debateDownVoted = debateDownVoted + :deltaDebateDownVoted "
        + "              WHERE accountId = :accountId "
        + "                AND zone = :zone "
        + "                AND bucket = :bucket "
        + "          RETURNING * "
        + "        ) "
        + " INSERT "
        + "   INTO RotateVoteStats "
        + "        (accountId, zone, bucket, username, debateCount, articleCount, articleUpVoted, "
        + "         debateUpVoted, debateDownVoted) "
        + " SELECT :accountId, :zone, :bucket, :username, :deltaDebateCount, :deltaArticleCount, "
        + "        :deltaArticleUpVoted,:deltaDebateUpVoted,:deltaDebateDownVoted "
        + "  WHERE NOT EXISTS (SELECT * FROM UpsertStats) ";

    Map<String, Object> params = ImmutableMap.<String, Object>builder()
        .put("accountId", accountId)
        .put("zone", zone.value())
        .put("bucket", bucket)
        .put("username", username)
        .put("deltaDebateCount", debateCount)
        .put("deltaArticleCount", articleCount)
        .put("deltaArticleUpVoted", articleUpVoted)
        .put("deltaDebateUpVoted", debateUpVoted)
        .put("deltaDebateDownVoted", debateDownVoted)
        .build();

    namedJdbc().update(upsert, params);
  }

  public void increaseArticleCount(Article article) {
    updateRotateVoteStats(article.getAuthorId(),
        article.getZone(),
        convertToBucket(article.getCreateTime()),
        article.getAuthorName(),
        0,
        1,
        0,
        0,
        0);
  }
}
