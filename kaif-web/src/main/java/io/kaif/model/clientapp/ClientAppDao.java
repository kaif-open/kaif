package io.kaif.model.clientapp;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import io.kaif.database.DaoOperations;
import io.kaif.model.account.Account;

@Repository
public class ClientAppDao implements DaoOperations {
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

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  public ClientApp createClientApp(Account creator,
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

  public ClientApp loadClientAppWithoutCache(String clientId) {
    return jdbc().queryForObject(" SELECT * FROM ClientApp WHERE clientId = ? ",
        clientAppMapper,
        clientId);
  }

  public List<ClientApp> listClientAppsOrderByTime(UUID ownerAccountId) {
    return jdbc().query(" SELECT * FROM ClientApp WHERE ownerAccountId = ? ORDER BY createTime ",
        clientAppMapper,
        ownerAccountId);
  }
}
