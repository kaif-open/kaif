package io.kaif.database;

import static java.util.stream.Collectors.*;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.common.base.Preconditions;

public interface DaoOperations {

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

  default Array createVarcharArray(Stream<String> collection) {
    return jdbc().execute((Connection con) -> {
      final String[] strings = collection.toArray(String[]::new);
      return con.createArrayOf("varchar", strings);
    });
  }

  default Stream<String> convertVarcharArray(Array array) {
    try {
      Stream.Builder<String> builder = Stream.builder();
      try (ResultSet arrayRs = array.getResultSet()) {
        while (arrayRs.next()) {
          builder.add(arrayRs.getString(2)); // index 1 is array index number
        }
      }
      return builder.build();
    } catch (SQLException e) {
      // see NamedParameterJdbcTemplate for source code
      throw ((JdbcTemplate) jdbc()).getExceptionTranslator()
          .translate("could not convert array: " + array, null, e);
    }
  }

  /**
   * generate questions placeholder like <code>(?,?,?)</code>. based on input count
   *
   * @param count
   *     must >= 1
   */
  default String questions(int count) {
    Preconditions.checkArgument(count > 0, "generate questions must at least 1");
    return " (" + IntStream.rangeClosed(1, count).mapToObj(i -> "?").collect(joining(",")) + ") ";
  }
}
