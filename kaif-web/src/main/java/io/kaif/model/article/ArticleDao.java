package io.kaif.model.article;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import io.kaif.database.DaoOperations;
import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.zone.Zone;

@Repository
public class ArticleDao implements DaoOperations {

  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Autowired
  private ArticleFlakeIdGenerator articleFlakeIdGenerator;

  private final RowMapper<Article> articleMapper = (rs, rowNum) -> {
    return new Article(//
        Zone.valueOf(rs.getString("zone")),
        FlakeId.valueOf(rs.getLong("articleId")),
        rs.getString("title"),
        rs.getString("urlName"),
        ArticleLinkType.valueOf(rs.getString("linkType")),
        rs.getTimestamp("createTime").toInstant(),
        rs.getString("content"),
        ArticleContentType.valueOf(rs.getString("contentType")),
        UUID.fromString(rs.getString("authorId")),
        rs.getString("authorName"),
        rs.getBoolean("deleted"),
        rs.getLong("upVote"),
        rs.getLong("downVote"),
        rs.getLong("debateCount"));
  };

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  private Article insertArticle(Article article) {
    jdbc().update(""
            + " INSERT "
            + "   INTO Article "
            + "        (zone, articleid, title, urlname, linktype, createtime, content, "
            + "         contenttype, authorid, authorname, deleted, upvote, downvote, debatecount)"
            + " VALUES "
            + questions(14),
        article.getZone().value(),
        article.getArticleId().value(),
        article.getTitle(),
        article.getUrlName(),
        article.getLinkType().name(),
        Timestamp.from(article.getCreateTime()),
        article.getContent(),
        article.getContentType().name(),
        article.getAuthorId(),
        article.getAuthorName(),
        article.isDeleted(),
        article.getUpVote(),
        article.getDownVote(),
        article.getDebateCount());
    return article;
  }

  public Optional<Article> findArticle(Zone zone, FlakeId articleId) {
    final String sql = " SELECT * FROM Article WHERE zone = ? AND articleId = ? LIMIT 1 ";
    return jdbc().query(sql, articleMapper, zone.value(), articleId.value()).stream().findAny();
  }

  public List<Article> listArticlesDesc(Zone zone, int offset, int limit) {
    final String sql = " SELECT * FROM Article WHERE zone = ? ORDER BY articleId DESC OFFSET ? LIMIT ? ";
    return jdbc().query(sql, articleMapper, zone.value(), offset, limit);
  }

  public Article createExternalLink(Zone zone,
      Account author,
      String title,
      String url,
      Instant now) {
    FlakeId flakeId = articleFlakeIdGenerator.next();
    return insertArticle(Article.createExternalLink(zone, flakeId, author, title, url, now));
  }

  /**
   * @throws EmptyResultDataAccessException
   *     if not found
   */
  public Article getArticle(Zone zone, FlakeId articleId) throws EmptyResultDataAccessException {
    final String sql = " SELECT * FROM Article WHERE zone = ? AND articleId = ? ";
    return jdbc().queryForObject(sql, articleMapper, zone.value(), articleId.value());
  }
}
