package io.kaif.model.vote;

import static java.util.stream.Collectors.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.ImmutableMap;

import io.kaif.database.DaoOperations;
import io.kaif.flake.FlakeId;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticleDao;

@Repository
public class VoteDao implements DaoOperations {

  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Autowired
  private ArticleDao articleDao;

  private final RowMapper<ArticleVoter> articleVoterMapper = (rs, rowNum) -> {
    return new ArticleVoter(UUID.fromString(rs.getString("voterId")),
        FlakeId.valueOf(rs.getLong("articleId")),
        VoteState.valueOf(rs.getString("voteState")),
        rs.getLong("previousCount"),
        rs.getTimestamp("updateTime").toInstant());
  };

  private final RowMapper<DebateVoter> debateVoterMapper = (rs, rowNum) -> {
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
        + "      INSERT "
        + "        INTO DebateVoter "
        + "             (voterId, articleId, debateId, previousCount, updateTime, voteState) "
        + "      VALUES (:voterId, :articleId, :debateId, :previousCount, :updateTime, :voteState) "
        + " ON CONFLICT (voterId, articleId, debateId) "
        + "   DO UPDATE "
        + "         SET previousCount = :previousCount "
        + "           , updateTime = :updateTime "
        + "           , voteState = :voteState "
        + "       WHERE DebateVoter.voteState = :previousState ";

    Map<String, Object> params = ImmutableMap.<String, Object>builder().put("voterId",
        voter.getVoterId())
        .put("debateId", voter.getDebateId().value())
        .put("articleId", voter.getArticleId().value())
        .put("previousCount", voter.getPreviousCount())
        .put("updateTime", Timestamp.from(voter.getUpdateTime()))
        .put("voteState", voter.getVoteState().name())
        .put("previousState", previousState.name())
        .build();

    /**
     * when inserted, rowAffected = 1
     * when conflict update, rowAffected = 1
     * when conflict update but previousState not match, rowAffected = 0
     */
    final int rowAffected = namedJdbc().update(upsert, params);
    if (rowAffected == 0) {
      throw new DuplicateKeyException("previousState: "
          + previousState
          + " not match exist DebateVoter.voteState");
    }
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
        + "      INSERT "
        + "        INTO ArticleVoter "
        + "             (voterId, articleId, previousCount, updateTime, voteState) "
        + "      VALUES (:voterId, :articleId, :previousCount, :updateTime, :voteState) "
        + " ON CONFLICT (voterid, articleId) "
        + "   DO UPDATE "
        + "         SET voteState = :voteState "
        + "           , previousCount = :previousCount "
        + "           , updateTime = :updateTime "
        + "       WHERE ArticleVoter.voteState = :previousState ";

    Map<String, Object> params = ImmutableMap.<String, Object>builder().put("voterId",
        voter.getVoterId())
        .put("articleId", voter.getArticleId().value())
        .put("previousCount", voter.getPreviousCount())
        .put("updateTime", Timestamp.from(voter.getUpdateTime()))
        .put("voteState", voter.getVoteState().name())
        .put("previousState", previousState.name())
        .build();

    final int rowAffected = namedJdbc().update(upsert, params);
    if (rowAffected == 0) {
      throw new DuplicateKeyException("previousState: "
          + previousState
          + " not match exist ArticleVoter.voteState");
    }
  }

  public List<ArticleVoter> listArticleVoters(UUID voterId, List<FlakeId> articleIds) {
    if (articleIds.isEmpty()) {
      return Collections.emptyList();
    }
    Map<String, Object> params = ImmutableMap.of("voterId",
        voterId,
        "articleIds",
        articleIds.stream().map(FlakeId::value).collect(toList()));
    return namedJdbc().query(""
        + " SELECT * "
        + "   FROM ArticleVoter "
        + "  WHERE voterId = :voterId "
        + "    AND articleId IN (:articleIds) ", params, articleVoterMapper);
  }

  public List<DebateVoter> listDebateVotersByIds(UUID voterId, List<FlakeId> debateIds) {
    if (debateIds.isEmpty()) {
      return Collections.emptyList();
    }
    Map<String, Object> params = ImmutableMap.of("voterId",
        voterId,
        "debateIds",
        debateIds.stream().map(FlakeId::value).collect(toList()));
    return namedJdbc().query(""
        + " SELECT * "
        + "   FROM DebateVoter"
        + "  WHERE voterId = :voterId "
        + "    AND debateId IN (:debateIds) ", params, debateVoterMapper);

  }

  /**
   * up voted articles are private to voter, so we don't exclude deleted articles
   */
  public List<Article> listUpVotedArticles(UUID voterId,
      @Nullable FlakeId startArticleId,
      int size) {
    FlakeId start = Optional.ofNullable(startArticleId).orElse(FlakeId.MAX);
    return jdbc().query(""
            + " SELECT a.* "
            + "   FROM ArticleVoter v "
            + "   JOIN Article a ON (v.articleId = a.articleId) "
            + "  WHERE v.voterId = ? "
            + "    AND v.articleId < ? "
            + "    AND v.voteState = ? "
            + "  ORDER BY v.articleId DESC "
            + "  LIMIT ? ",
        articleDao.getArticleMapper(),
        voterId,
        start.value(),
        VoteState.UP.name(),
        size);
  }
}
