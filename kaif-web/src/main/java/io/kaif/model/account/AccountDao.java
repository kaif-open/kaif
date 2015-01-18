package io.kaif.model.account;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import io.kaif.database.DaoOperations;

@Repository
public class AccountDao implements DaoOperations {

  private final RowMapper<Account> accountMapper = (rs,
      rowNum) -> new Account(UUID.fromString(rs.getString("accountId")),
      rs.getString("name"),
      rs.getString("email"),
      rs.getString("passwordHash"),
      rs.getTimestamp("createTime").toInstant(),
      convertVarcharArray(rs.getArray("authorities")).map(Authority::valueOf)
          .collect(Collectors.toSet()),
      rs.getBoolean("activated"));

  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  public Account create(String name, String email, String passwordHash) {

    final Account account = Account.create(name, email, passwordHash, Instant.now());

    Stream<String> authString = account.getAuthorities().stream().map(Authority::name);
    jdbc().update(""
            + " INSERT "
            + "   INTO Account "
            + "        (accountId, email, passwordHash, name, "
            + "         createTime, activated, authorities) "
            + " VALUES "
            + questions(7),
        account.getAccountId(),
        account.getEmail(),
        account.getPasswordHash(),
        account.getName(),
        Timestamp.from(account.getCreateTime()),
        account.isActivated(),
        createVarcharArray(authString));

    return account;
  }

  public Account findById(UUID accountId) {
    final String sql = " SELECT * FROM Account WHERE accountId = ? ";
    return jdbc().query(sql, accountMapper, accountId).stream().findAny().orElse(null);
  }
}
