package io.kaif.model.article;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import io.kaif.database.DaoOperations;
import io.kaif.flake.FlakeId;
import io.kaif.model.KaifIdGenerator;

@Repository
public class ArticleWatchDao implements DaoOperations {
  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Autowired
  private KaifIdGenerator kaifIdGenerator;

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  public List<ArticleWatch> listWatched(UUID accountId, @Nullable FlakeId startWatchId, int size) {
    return null;
  }

  public Optional<ArticleWatch> watch(UUID accountId, FlakeId articleId, Instant now) {
    //TODO use upsert, and ignore duplicate
    return null;
  }

  public List<UUID> listWatchers(FlakeId articleId) {
    //TODO paging per 2000 watchers
    return null;
  }

}
