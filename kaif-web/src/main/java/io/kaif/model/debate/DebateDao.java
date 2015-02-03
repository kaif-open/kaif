package io.kaif.model.debate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import io.kaif.database.DaoOperations;
import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.article.Article;

@Repository
public class DebateDao implements DaoOperations {

  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Autowired
  private DebateFlakeIdGenerator debateFlakeIdGenerator;

  private final RowMapper<Debate> debateMapper = (rs, rowNum) -> {

    return new Debate(FlakeId.valueOf(rs.getLong("articleId")),
        FlakeId.valueOf(rs.getLong("debateId")),
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
            + "        (articleid, debateid, parentdebateid, level, content, contenttype, "
            + "         debaterid, debatername, upvote, downvote, createtime, lastupdatetime)"
            + " VALUES "
            + questions(12),
        debate.getArticleId().value(),
        debate.getDebateId().value(),
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

  public Optional<Debate> findDebate(FlakeId articleId, FlakeId debateId) {
    if (Debate.NO_PARENT.equals(debateId)) {
      return Optional.empty();
    }
    final String sql = " SELECT * FROM Debate WHERE articleId = ? AND debateId = ? LIMIT 1 ";
    return jdbc().query(sql, debateMapper, articleId.value(), debateId.value()).stream().findAny();
  }

  public Debate create(Article article,
      @Nullable Debate parent,
      String content,
      Account debater,
      Instant now) {
    return insertDebate(Debate.create(article,
        debateFlakeIdGenerator.next(),
        parent,
        content,
        debater,
        now));
  }
}
