package io.kaif.database;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.util.stream.Collectors.*;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.common.base.Preconditions;

import io.kaif.flake.FlakeId;

public interface DaoOperations {

  @FunctionalInterface
  public interface CheckedSqlBiFunction<T, U, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t
     *     the first function argument
     * @param u
     *     the second function argument
     * @return the function result
     */
    R apply(T t, U u) throws SQLException;
  }

  ZoneId ZONE_TAIPEI = ZoneId.of("Asia/Taipei");

  NamedParameterJdbcTemplate namedJdbc();

  default JdbcOperations jdbc() {
    return namedJdbc().getJdbcOperations();
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
    return createArray(collection, "varchar");
  }

  default Array createUuidArray(Stream<UUID> collection) {
    return createArray(collection, "uuid");
  }

  default <T> Array createArray(Stream<T> collection, String sqlType) {
    return jdbc().execute((Connection con) -> {
      return con.createArrayOf(sqlType, collection.toArray());
    });
  }

  default <T> Stream<T> convertArray(Array array,
      CheckedSqlBiFunction<ResultSet, Integer, T> extract) {
    try {
      Stream.Builder<T> builder = Stream.builder();
      try (ResultSet arrayRs = array.getResultSet()) {
        while (arrayRs.next()) {
          // index 1 is array index number, so extract should use 2
          builder.add(extract.apply(arrayRs, 2));
        }
      }
      return builder.build();
    } catch (SQLException e) {
      // see NamedParameterJdbcTemplate for source code
      throw ((JdbcTemplate) jdbc()).getExceptionTranslator()
          .translate("could not convert array: " + array, null, e);
    }

  }

  default Stream<String> convertVarcharArray(Array array) {
    return convertArray(array, ResultSet::getString);
  }

  default Stream<UUID> convertUuidArray(Array array) {
    return convertArray(array,
        (resultSet, columnIndex) -> UUID.fromString(resultSet.getString(columnIndex)));
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

  default LocalDate monthlyBucket(Instant instant) {
    return instant.atZone(ZONE_TAIPEI).with(firstDayOfMonth()).toLocalDate();
  }

  default LocalDate monthlyBucket(FlakeId flakeId) {
    return monthlyBucket(Instant.ofEpochMilli(flakeId.epochMilli()));
  }

}
