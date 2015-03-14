package io.kaif.model.vote;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.ImmutableMap;

import io.kaif.database.DaoOperations;
import io.kaif.model.zone.Zone;

@Repository
public class RotateVoteStatsDao implements DaoOperations {
  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private RowMapper<RotateVoteStats> rotateVoteStatsMapper = (rs, rowNum) -> new RotateVoteStats(
      UUID.fromString(rs.getString("accountId")),
      Zone.valueOf(rs.getString("zone")),
      rs.getString("bucket"),
      rs.getString("username"),
      rs.getLong("debateCount"),
      rs.getLong("articleCount"),
      rs.getLong("articleUpVoted"),
      rs.getLong("debateUpVoted"),
      rs.getLong("debateDownVoted")
  );

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  public Optional<RotateVoteStats> findRotateVoteStats(UUID accountId, Zone zone, Instant instant) {
    final String sql = " SELECT * FROM RotateVoteStats WHERE accountId = ? AND zone = ? AND bucket = ? LIMIT 1 ";
    return jdbc().query(sql,
        rotateVoteStatsMapper,
        accountId,
        zone.value(),
        monthlyBucket(instant).toString())
        .stream()
        .findAny();
  }

  public void updateRotateVoteStats(HonorRollVoter voter) {
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
        .put("accountId", voter.getAccountId())
        .put("zone", voter.getZone().value())
        .put("bucket", monthlyBucket(voter.getFlakeId()).toString())
        .put("username", voter.getUsername())
        .put("deltaDebateCount", voter.getDeltaDebateCount())
        .put("deltaArticleCount", voter.getDeltaArticleCount())
        .put("deltaArticleUpVoted", voter.getDeltaArticleUpVoted())
        .put("deltaDebateUpVoted", voter.getDeltaDebateUpVoted())
        .put("deltaDebateDownVoted", voter.getDeltaDebateDownVoted())
        .build();

    namedJdbc().update(upsert, params);
  }

  public List<RotateVoteStats> listRotateVoteStatsByAccount(UUID uuid, Instant instant) {
    final String sql = " SELECT * FROM RotateVoteStats WHERE accountId = ? AND bucket = ? ORDER BY zone";
    return jdbc().query(sql,
        rotateVoteStatsMapper,
        uuid,
        monthlyBucket(instant).toString());
  }

  public List<RotateVoteStats> listRotateVoteStatsByZone(Zone zone, Instant instant, int limit) {
    final String sql = " SELECT * FROM RotateVoteStats WHERE zone = ? AND bucket = ? ORDER BY articleUpVoted + debateUpVoted - debateDownVoted DESC, username ASC LIMIT ? ";
    return jdbc().query(sql,
        rotateVoteStatsMapper,
        zone.value(),
        monthlyBucket(instant).toString(), limit);
  }
}
