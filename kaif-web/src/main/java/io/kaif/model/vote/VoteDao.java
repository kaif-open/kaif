package io.kaif.model.vote;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
        VoteState.valueOf(rs.getString("voteState")),
        rs.getLong("previousCount"),
        rs.getTimestamp("updateTime").toInstant());
  };

  private RowMapper<DebateVoter> debateVoterMapper = (rs, rowNum) -> {
    return new DebateVoter(UUID.fromString(rs.getString("voterId")),
        FlakeId.valueOf(rs.getLong("articleId")),
        FlakeId.valueOf(rs.getLong("debateId")),
        VoteState.valueOf(rs.getString("voteState")),
        rs.getLong("previousCount"),
        rs.getTimestamp("updateTime").toInstant());
  };

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  public List<ArticleVoter> listArticleVotersInRage(UUID accountId,
      FlakeId oldestArticleId,
      FlakeId newestArticleId) {
    return jdbc().query(""
            + " SELECT * "
            + "   FROM ArticleVoter "
            + "  WHERE voterId = ? "
            + "    AND articleId BETWEEN ? AND ? ",
        articleVoterMapper,
        accountId,
        oldestArticleId.value(),
        newestArticleId.value());
  }

  public void voteDebate(VoteState newState,
      FlakeId articleId,
      FlakeId debateId,
      UUID voterId,
      VoteState previousState,
      long previousCount,
      Instant now) throws DuplicateKeyException {

    DebateVoter voter = DebateVoter.create(newState,
        articleId,
        debateId,
        voterId,
        previousCount,
        now);

    // allow two cases:
    //
    // 1) no data: just do INSERT
    // 2) a vote exist match previous state: update to new vote state
    //
    // note that we do not allow update previousState not match.

    String upsert = ""
        + "   WITH UpsertVote "
        + "     AS ("
        + "             UPDATE DebateVoter "
        + "                SET previousCount = :previousCount "
        + "                  , updateTime = :updateTime "
        + "                  , voteState = :voteState "
        + "              WHERE articleId = :articleId "
        + "                AND voterId = :voterId "
        + "                AND debateId = :debateId "
        + "                AND voteState = :previousState "
        + "          RETURNING * "
        + "        ) "
        + " INSERT "
        + "   INTO DebateVoter "
        + "        (voterId, articleId, debateId, previousCount, updateTime, voteState) "
        + " SELECT :voterId, :articleId, :debateId, :previousCount, :updateTime, :voteState "
        + "  WHERE NOT EXISTS (SELECT * FROM UpsertVote) ";

    Map<String, Object> params = ImmutableMap.<String, Object>builder()
        .put("voterId", voter.getVoterId())
        .put("debateId", voter.getDebateId().value())
        .put("articleId", voter.getArticleId().value())
        .put("previousCount", voter.getPreviousCount())
        .put("updateTime", Timestamp.from(voter.getUpdateTime()))
        .put("voteState", voter.getVoteState().name())
        .put("previousState", previousState.name())
        .build();

    namedJdbc().update(upsert, params);
  }

  public List<DebateVoter> listDebateVotersByArticle(UUID accountId, FlakeId articleId) {
    return jdbc().query(""
        + " SELECT * "
        + "   FROM DebateVoter "
        + "  WHERE voterId = ? "
        + "    AND articleId = ? ", debateVoterMapper, accountId, articleId.value());
  }

  public void voteArticle(VoteState newState,
      FlakeId articleId,
      UUID voterId,
      VoteState previousState,
      long previousCount,
      Instant now) throws DuplicateKeyException {

    ArticleVoter voter = ArticleVoter.create(newState, articleId, voterId, previousCount, now);

    /*
     * implementation similar to voteDebate, see document there for explanation
     */
    String upsert = ""
        + "   WITH UpsertVote "
        + "     AS ("
        + "             UPDATE ArticleVoter "
        + "                SET previousCount = :previousCount "
        + "                  , updateTime = :updateTime "
        + "                  , voteState = :voteState "
        + "              WHERE articleId = :articleId "
        + "                AND voterId = :voterId "
        + "                AND voteState = :previousState "
        + "          RETURNING * "
        + "        ) "
        + " INSERT "
        + "   INTO ArticleVoter "
        + "        (voterId, articleId, previousCount, updateTime, voteState) "
        + " SELECT :voterId, :articleId, :previousCount, :updateTime, :voteState "
        + "  WHERE NOT EXISTS (SELECT * FROM UpsertVote) ";

    Map<String, Object> params = ImmutableMap.<String, Object>builder()
        .put("voterId", voter.getVoterId())
        .put("articleId", voter.getArticleId().value())
        .put("previousCount", voter.getPreviousCount())
        .put("updateTime", Timestamp.from(voter.getUpdateTime()))
        .put("voteState", voter.getVoteState().name())
        .put("previousState", previousState.name())
        .build();

    namedJdbc().update(upsert, params);
  }
}
