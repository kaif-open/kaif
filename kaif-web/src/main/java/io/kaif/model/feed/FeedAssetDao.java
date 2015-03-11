package io.kaif.model.feed;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import io.kaif.database.DaoOperations;
import io.kaif.flake.FlakeId;
import io.kaif.model.KaifIdGenerator;

@Repository
public class FeedAssetDao implements DaoOperations {

  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Autowired
  private KaifIdGenerator kaifIdGenerator;

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  public void insertFeed(FeedAsset feed) {
  }

  public List<FeedAsset> listFeeds(UUID accountId, FlakeId startId, int size) {
    return null;
  }

  public void acknowledge(UUID accountId) {

  }
}
