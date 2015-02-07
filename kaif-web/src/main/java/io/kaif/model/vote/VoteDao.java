package io.kaif.model.vote;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.ImmutableMap;

import io.kaif.database.DaoOperations;
import io.kaif.flake.FlakeId;

@Repository
public class VoteDao implements DaoOperations {
  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private RowMapper<ArticleVoter> articleVoterMapper = (rs, rowNum) -> {
    return new ArticleVoter(UUID.fromString(rs.getString("voterId")),
        FlakeId.valueOf(rs.getLong("articleId")),
        rs.getBoolean("cancel"),
        rs.getLong("previousCount"),
        rs.getTimestamp("updateTime").toInstant());
  };

  public VoteDelta upVoteArticle(FlakeId articleId,
      UUID accountId,
      long previousCount,
      Instant now) {
    ArticleVoter voter = ArticleVoter.upVote(articleId, accountId, previousCount, now);

    // allow two cases:
    //
    // 1) no data: just do INSERT
    // 2) a canceled vote exist: update to voted again
    //
    // thus, the result of success execution always mean vote + 1
    //
    // note that we do not allow update a non-canceled voter.

    String upsert = ""
        + "   WITH UpsertVote "
        + "     AS ("
        + "             UPDATE ArticleVoter "
        + "                SET cancel = :cancel"
        + "                  , previousCount = :previousCount"
        + "                  , updateTime = :updateTime "
        + "              WHERE articleId = :articleId "
        + "                AND voterId = :voterId "
        + "                AND cancel = TRUE "
        + "          RETURNING * "
        + "        ) "
        + " INSERT "
        + "   INTO ArticleVoter "
        + "        (voterId, articleId, cancel, previousCount, updateTime) "
        + " SELECT :voterId, :articleId, :cancel, :previousCount, :updateTime "
        + "  WHERE NOT EXISTS (SELECT * FROM UpsertVote) ";

    Map<String, Object> params = ImmutableMap.of("voterId",
        voter.getVoterId(),
        "articleId",
        voter.getArticleId().value(),
        "cancel",
        voter.isCancel(),
        "previousCount",
        voter.getPreviousCount(),
        "updateTime",
        Timestamp.from(voter.getUpdateTime()));

    namedJdbc().update(upsert, params);
    return VoteDelta.INCREASED;
  }

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  public List<ArticleVoter> listArticleVotersInRage(UUID accountId,
      FlakeId startArticleId,
      FlakeId endArticleId) {
    return jdbc().query(""
            + " SELECT * "
            + "   FROM ArticleVoter "
            + "  WHERE voterId = ? "
            + "    AND articleId BETWEEN ? AND ? ",
        articleVoterMapper,
        accountId,
        startArticleId.value(),
        endArticleId.value());
  }

  public VoteDelta cancelVoteArticle(FlakeId articleId, UUID accountId, Instant now) {
    int changed = jdbc().update(""
        + " UPDATE ArticleVoter "
        + "    SET cancel = ? "
        + "      , previousCount = 0 "
        + "      , updateTime = ? "
        + "  WHERE voterId = ? "
        + "    AND articleId = ? ", true, Timestamp.from(now), accountId, articleId.value());
    return changed == 1 ? VoteDelta.DECREASED : VoteDelta.NO_CHANGE;
  }
}
