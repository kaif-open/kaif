package io.kaif.model.zone;

import static java.util.stream.Collectors.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import io.kaif.database.DaoOperations;
import io.kaif.model.account.Authority;

/**
 * @see io.kaif.config.UtilConfiguration#zoneInfoCacheManager()
 */
@Repository
@CacheConfig(cacheNames = "ZoneInfo")
public class ZoneDao implements DaoOperations {

  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private final RowMapper<ZoneInfo> zoneInfoMapper = (rs, n) -> {
    List<UUID> adminAccountIds = convertUuidArray(rs.getArray("adminAccountIds")).collect(toList());
    return new ZoneInfo(//
        Zone.valueOf(rs.getString("zone")),
        rs.getString("aliasName"),
        rs.getString("theme"),
        Authority.valueOf(rs.getString("voteAuthority")),
        Authority.valueOf(rs.getString("debateAuthority")),
        Authority.valueOf(rs.getString("writeAuthority")),
        adminAccountIds,
        rs.getBoolean("hideFromTop"),
        rs.getTimestamp("createTime").toInstant());
  };

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  public ZoneInfo create(ZoneInfo zoneInfo) {
    jdbc().update(""
            + " INSERT "
            + "   INTO ZoneInfo "
            + "        (zone, aliasName, theme, voteAuthority, debateAuthority, writeAuthority, "
            + "         createTime, adminAccountIds, hideFromTop) "
            + " VALUES "
            + questions(9),
        zoneInfo.getZone().value(),
        zoneInfo.getAliasName(),
        zoneInfo.getTheme(),
        zoneInfo.getVoteAuthority().name(),
        zoneInfo.getDebateAuthority().name(),
        zoneInfo.getWriteAuthority().name(),
        Timestamp.from(zoneInfo.getCreateTime()),
        createUuidArray(zoneInfo.getAdminAccountIds().stream()),
        zoneInfo.isHideFromTop());
    return zoneInfo;
  }

  public ZoneInfo getZoneWithoutCache(Zone zone) throws EmptyResultDataAccessException {
    return jdbc().queryForObject("SELECT * FROM ZoneInfo WHERE zone = ? ",
        zoneInfoMapper,
        zone.value());
  }

  //use argument `zone` as cache key
  @Cacheable
  public ZoneInfo getZone(Zone zone) throws EmptyResultDataAccessException {
    return getZoneWithoutCache(zone);
  }

  @CacheEvict(key = "#a0") //a0 is first argument
  public void updateTheme(Zone zone, String theme) {
    jdbc().update("UPDATE ZoneInfo SET theme = ? WHERE zone = ? ", theme, zone.value());
  }
}
