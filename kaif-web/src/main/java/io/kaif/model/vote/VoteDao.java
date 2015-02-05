package io.kaif.model.vote;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

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
    // if exist
    // update , set cancel = false
    // or insert
    ArticleVoter voter = ArticleVoter.upVote(articleId, accountId, previousCount, now);

    jdbc().update(""
            + " INSERT "
            + "   INTO ArticleVoter "
            + "        (voterid, articleid, cancel, previouscount, updatetime) "
            + "  VALUES "
            + questions(5),
        voter.getVoterId(),
        voter.getArticleId().value(),
        voter.isCancel(),
        voter.getPreviousCount(),
        Timestamp.from(voter.getUpdateTime()));
    return VoteDelta.INCREASED;
  }

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  public List<ArticleVoter> listArticleVotersAfter(UUID accountId, Instant updateTime) {
    return jdbc().query(""
            + " SELECT * "
            + "   FROM ArticleVoter "
            + "  WHERE voterId = ? "
            + "    AND updateTime >= ? "
            + "  ORDER BY updateTime ASC ",
        articleVoterMapper,
        accountId,
        Timestamp.from(updateTime));
  }
}
