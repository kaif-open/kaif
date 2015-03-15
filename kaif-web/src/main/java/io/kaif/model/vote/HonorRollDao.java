package io.kaif.model.vote;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import io.kaif.database.DaoOperations;
import io.kaif.model.zone.Zone;

@Repository
public class HonorRollDao implements DaoOperations {
  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private final RowMapper<HonorRoll> honorRollRowMapper = (rs,
      rowNum) -> new HonorRoll(UUID.fromString(rs.getString("accountId")),
      rs.getString("zone") == null ? null : Zone.valueOf(rs.getString("zone")),
      // for total
      rs.getString("bucket"),
      rs.getString("username"),
      rs.getLong("articleUpVoted"),
      rs.getLong("debateUpVoted"),
      rs.getLong("debateDownVoted"));

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  public Optional<HonorRoll> findHonorRoll(UUID accountId, Zone zone, Instant instant) {
    final String sql = ""
        + " SELECT * "
        + "   FROM HonorRoll "
        + "  WHERE accountId = ? "
        + "    AND zone = ? "
        + "    AND bucket = ? "
        + "  LIMIT 1 ";
    return jdbc().query(sql,
        honorRollRowMapper,
        accountId,
        zone.value(),
        monthlyBucket(instant).toString()).stream().findAny();
  }

  public void updateRotateVoteStats(HonorRollVoter voter) {
    String upsert = ""
        + "   WITH Upsert "
        + "     AS ("
        + "             UPDATE HonorRoll "
        + "                SET articleUpVoted = articleUpVoted + :deltaArticleUpVoted "
        + "                  , debateUpVoted = debateUpVoted + :deltaDebateUpVoted "
        + "                  , debateDownVoted = debateDownVoted + :deltaDebateDownVoted "
        + "              WHERE accountId = :accountId "
        + "                AND zone = :zone "
        + "                AND bucket = :bucket "
        + "          RETURNING * "
        + "        ) "
        + " INSERT "
        + "   INTO HonorRoll "
        + "        (accountId, zone, bucket, username, articleUpVoted, "
        + "         debateUpVoted, debateDownVoted) "
        + " SELECT :accountId, :zone, :bucket, :username, "
        + "        :deltaArticleUpVoted,:deltaDebateUpVoted,:deltaDebateDownVoted "
        + "  WHERE NOT EXISTS (SELECT * FROM Upsert) ";

    Map<String, Object> params = ImmutableMap.<String, Object>builder()
        .put("accountId", voter.getAccountId())
        .put("zone", voter.getZone().value())
        .put("bucket", monthlyBucket(voter.getFlakeId()).toString())
        .put("username", voter.getUsername())
        .put("deltaArticleUpVoted", voter.getDeltaArticleUpVoted())
        .put("deltaDebateUpVoted", voter.getDeltaDebateUpVoted())
        .put("deltaDebateDownVoted", voter.getDeltaDebateDownVoted())
        .build();

    namedJdbc().update(upsert, params);
  }

  public List<HonorRoll> listHonorRollByAccount(UUID uuid, Instant instant) {
    final String sql = " SELECT * FROM HonorRoll WHERE accountId = ? AND bucket = ? ORDER BY zone ";
    return jdbc().query(sql, honorRollRowMapper, uuid, monthlyBucket(instant).toString());
  }

  /**
   * see {@link #listHonorRollWithCache(java.time.LocalDate, int)} for why leak bucket to outside
   */
  @Cacheable(value = "listHonorRoll")
  public List<HonorRoll> listHonorRollByZoneWithCache(Zone zone, LocalDate bucket, int limit) {
    final String sql = ""
        + " SELECT * "
        + "   FROM HonorRoll "
        + "  WHERE zone = ? "
        + "    AND bucket = ? "
        + "  ORDER BY (articleUpVoted + debateUpVoted - debateDownVoted) DESC, username ASC "
        + "  LIMIT ? ";
    return jdbc().query(sql, honorRollRowMapper, zone.value(), bucket.toString(), limit);
  }

  /**
   * argument using `LocalDate bucket` leak rotation logic outside of Dao,
   * But to make @Cacheable work properly. we have to do so.
   */
  @Cacheable(value = "listHonorRoll")
  public List<HonorRoll> listHonorRollWithCache(LocalDate bucket, int limit) {
    final String sql = ""
        + " SELECT accountId, bucket, username, "
        + "        NULL AS zone, "
        + "        sum(articleUpVoted) AS articleUpVoted, "
        + "        sum(debateUpVoted) AS debateUpVoted, "
        + "        sum(debateDownVoted) AS debateDownVoted, "
        + "        sum(articleUpVoted) + sum(debateUpVoted) - sum(debateDownVoted) AS score "
        + "   FROM HonorRoll "
        + "  WHERE bucket = ? "
        + "  GROUP BY accountId, bucket, username "
        + "  ORDER BY score DESC, username ASC "
        + "  LIMIT ? ";
    return jdbc().query(sql, honorRollRowMapper, bucket.toString(), limit);
  }

  @VisibleForTesting
  @CacheEvict(value = "listHonorRoll", allEntries = true)
  public void evictAllCaches() {
  }
}
