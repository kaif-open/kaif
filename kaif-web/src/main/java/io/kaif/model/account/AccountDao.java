package io.kaif.model.account;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import io.kaif.database.DaoOperation;

@Repository
public class AccountDao implements DaoOperation {

  private final RowMapper<Account> accountMapper = (rs, rowNum) -> new Account(rs.getString("name"),
      UUID.fromString(rs.getString("accountId")),
      rs.getString("email"),
      rs.getString("passwordHash"),
      rs.getTimestamp("createTime").toInstant(),
      EnumSet.of(Authority.NORMAL));

  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  public Account create(String email, String passwordHash, String name) {

    final Account account = Account.create(email, passwordHash, name, Instant.now());

    jdbc().update(""
            + " INSERT INTO Account (accountId, email, passwordHash, name, "
            + "                      createTime) "
            + " VALUES (?,?,?,?,?) ",
        account.getAccountId(),
        account.getEmail(),
        account.getPasswordHash(),
        account.getName(),
        Timestamp.from(account.getCreateTime()));

    return account;
  }

  public Account findById(UUID accountId) {
    final String sql = " SELECT * FROM Account WHERE accountId = ? ";
    return jdbc().query(sql, accountMapper, accountId).stream().findAny().orElse(null);
  }
}
