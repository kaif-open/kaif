package io.kaif.database;

import java.sql.SQLException;
import java.util.UUID;

import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public interface DaoOperation {

  NamedParameterJdbcTemplate namedJdbc();

  default JdbcOperations jdbc() {
    return namedJdbc().getJdbcOperations();
  }

  default UUID uuid(String id) {
    return UUID.fromString(id);
  }

  default PGobject pgJson(final String rawJson) {
    final PGobject jsonObject = new PGobject();
    jsonObject.setType("json");
    try {
      jsonObject.setValue(rawJson);
      return jsonObject;
    } catch (final SQLException e) {
      // see NamedParameterJdbcTemplate for source code
      throw ((JdbcTemplate) jdbc()).getExceptionTranslator()
          .translate("not valid json: " + rawJson, null, e);
    }
  }
}
