package io.kaif.model.feed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
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

  private final RowMapper<FeedAsset> feedAssetRowMapper = new RowMapper<FeedAsset>() {
    @Override
    public FeedAsset mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new FeedAsset(UUID.fromString(rs.getString("accountId")),
          FlakeId.valueOf(rs.getLong("assetId")),
          FeedAsset.AssetType.fromIndex(rs.getInt("assetType")),
          rs.getTimestamp("createTime").toInstant(),
          rs.getBoolean("acked"));
    }
  };

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  public FeedAsset insertFeed(FeedAsset feed) {
    jdbc().update(""
            + "  INSERT INTO FeedAsset (accountId, assetId, assetType, createTime, acked) "
            + "  VALUES "
            + questions(5),
        feed.getAccountId(),
        feed.getAssetId().value(),
        feed.getAssetType().getIndex(),
        Timestamp.from(feed.getCreateTime()),
        feed.isAcked());
    return feed;
  }

  public List<FeedAsset> listFeedsDesc(UUID accountId, @Nullable FlakeId startId, int size) {
    FlakeId startAssetId = Optional.ofNullable(startId).orElse(FlakeId.MAX);
    return jdbc().query(""
        + " SELECT * "
        + "   FROM FeedAsset "
        + "  WHERE accountId = ? "
        + "    AND assetId < ? "
        + "  ORDER BY assetId DESC "
        + "  LIMIT ? ", feedAssetRowMapper, accountId, startAssetId.value(), size);
  }

  public void acknowledge(UUID accountId) {

  }
}
