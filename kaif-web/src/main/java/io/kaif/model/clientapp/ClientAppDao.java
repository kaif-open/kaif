package io.kaif.model.clientapp;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import io.kaif.database.DaoOperations;
import io.kaif.model.account.Account;

@Repository
public class ClientAppDao implements DaoOperations {

  private static final String FIND_CLIENT_APP_USER_CACHE_NAME = "findClientAppUser";

  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private final RowMapper<ClientApp> clientAppMapper = (rs, num) -> {
    return new ClientApp(rs.getString("clientId"),
        rs.getString("clientSecret"),
        rs.getString("appName"),
        rs.getString("description"),
        rs.getTimestamp("createTime").toInstant(),
        UUID.fromString(rs.getString("ownerAccountId")),
        rs.getBoolean("revoked"),
        rs.getString("callbackUri"));
  };

  private final RowMapper<ClientAppUser> clientAppUserMapper = (rs, num) -> {

    Set<ClientAppScope> lastGrantedScopes = convertVarcharArray(rs.getArray("lastGrantedScopes")).map(
        ClientAppScope::valueOf).collect(Collectors.toSet());

    return new ClientAppUser(UUID.fromString(rs.getString("clientAppUserId")),
        rs.getString("clientId"),
        rs.getString("clientSecret"),
        UUID.fromString(rs.getString("accountId")),
        lastGrantedScopes,
        rs.getTimestamp("lastUpdateTime").toInstant());
  };

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  public ClientApp createApp(Account creator,
      String name,
      String description,
      String callbackUri,
      Instant now) {

    ClientApp app = ClientApp.create(creator.authenticatedId(),
        name,
        description,
        callbackUri,
        now);
    jdbc().update(""
            + " INSERT "
            + "   INTO ClientApp "
            + "        (clientId, clientSecret, appName, description, createTime, "
            + "         ownerAccountId, revoked, callbackUri) "
            + " VALUES "
            + questions(8),
        app.getClientId(),
        app.getClientSecret(),
        app.getAppName(),
        app.getDescription(),
        Timestamp.from(app.getCreateTime()),
        app.getOwnerAccountId(),
        app.isRevoked(),
        app.getCallbackUri());
    return app;
  }

  public ClientApp loadAppWithoutCache(String clientId) {
    return jdbc().queryForObject(" SELECT * FROM ClientApp WHERE clientId = ? ",
        clientAppMapper,
        clientId);
  }

  public List<ClientApp> listAppOrderByTime(UUID ownerAccountId) {
    return jdbc().query(" SELECT * FROM ClientApp WHERE ownerAccountId = ? ORDER BY createTime ",
        clientAppMapper,
        ownerAccountId);
  }

  public void updateAppInformation(ClientApp updated) {
    jdbc().update(""
            + " UPDATE ClientApp "
            + "    SET appName = ? "
            + "      , description = ? "
            + "      , revoked = ? "
            + "      , callbackUri = ? "
            + "  WHERE clientId = ? ",
        updated.getAppName(),
        updated.getDescription(),
        updated.isRevoked(),
        updated.getCallbackUri(),
        updated.getClientId());
  }

  /*
 * there is no way to evict app's all users, so we have to evict all entries
 */
  @CacheEvict(value = FIND_CLIENT_APP_USER_CACHE_NAME, allEntries = true)
  public void updateAppSecret(ClientApp updated) {
    jdbc().update("" + " UPDATE ClientApp " + "    SET clientSecret = ? " + "  WHERE clientId = ? ",
        updated.getClientSecret(),
        updated.getClientId());
  }

  public Optional<ClientApp> findApp(String clientId) {
    return jdbc().query(" SELECT * FROM ClientApp WHERE clientId = ? LIMIT 1",
        clientAppMapper,
        clientId).stream().findAny();
  }

  @CacheEvict(value = FIND_CLIENT_APP_USER_CACHE_NAME, key = "#a0.accountId + #a1.clientId")
  public ClientAppUser mergeClientAppUser(Account account,
      ClientApp clientApp,
      Set<ClientAppScope> scopes,
      Instant now) {
    Optional<ClientAppUser> exist = findClientAppUserWithoutCache(account.getAccountId(),
        clientApp.getClientId());

    if (exist.isPresent()) {
      ClientAppUser updated = exist.get()
          .withScopes(scopes)
          .withLastUpdateTime(now)
          .withClientSecret(clientApp.getClientSecret());
      jdbc().update(""
              + " UPDATE ClientAppUser "
              + "    SET lastGrantedScopes = ? "
              + "      , lastUpdateTime = ? "
              + "  WHERE clientAppUserId = ? ",
          createVarcharArray(updated.getLastGrantedScopes().stream().map(ClientAppScope::name)),
          Timestamp.from(updated.getLastUpdateTime()),
          updated.getClientAppUserId());
      return updated;
    } else {
      ClientAppUser created = ClientAppUser.create(clientApp.getClientId(),
          clientApp.getClientSecret(),
          account.getAccountId(),
          scopes,
          now);
      jdbc().update(""
              + " INSERT "
              + "   INTO ClientAppUser "
              + "        (clientAppUserId, clientId, accountId, lastGrantedScopes, lastUpdateTime )"
              + " VALUES "
              + questions(5),
          created.getClientAppUserId(),
          created.getClientId(),
          created.getAccountId(),
          createVarcharArray(created.getLastGrantedScopes().stream().map(ClientAppScope::name)),
          Timestamp.from(created.getLastUpdateTime()));
      return created;
    }
  }

  @Cacheable(value = FIND_CLIENT_APP_USER_CACHE_NAME, key = "#a0 + #a1")
  public Optional<ClientAppUser> findClientAppUserWithCache(UUID accountId, String clientId) {
    return findClientAppUserWithoutCache(accountId, clientId);
  }

  public Optional<ClientAppUser> findClientAppUserWithoutCache(UUID accountId, String clientId) {
    return jdbc().query(""
        + " SELECT cau.*, ClientApp.clientSecret "
        + "   FROM ClientAppUser cau "
        + "   JOIN ClientApp ON (cau.clientId = ClientApp.clientId ) "
        + "  WHERE cau.accountId = ? "
        + "    AND cau.clientId = ? "
        + "  LIMIT 1 ", clientAppUserMapper, accountId, clientId).stream().findAny();
  }

  public List<ClientAppUser> listAppsByUser(UUID accountId) {
    return jdbc().query(""
        + " SELECT cau.*, ClientApp.clientSecret "
        + "   FROM ClientAppUser cau "
        + "   JOIN ClientApp ON (cau.clientId = ClientApp.clientId ) "
        + "  WHERE cau.accountId = ? ", clientAppUserMapper, accountId);
  }

  @CacheEvict(value = FIND_CLIENT_APP_USER_CACHE_NAME, key = "#a0 + #a1")
  public void deleteClientAppUser(UUID accountId, String clientId) {
    jdbc().update(""
        + " DELETE "
        + "   FROM ClientAppUser "
        + "  WHERE accountId = ? "
        + "    AND clientId = ? ", accountId, clientId);
  }
}
