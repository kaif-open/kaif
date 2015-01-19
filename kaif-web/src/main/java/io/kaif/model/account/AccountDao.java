package io.kaif.model.account;

import java.sql.Array;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
        authoritiesToVarcharArray(account.getAuthorities()));

    return account;
  }

  private Array authoritiesToVarcharArray(Set<Authority> authorities) {
    return createVarcharArray(authorities.stream().map(Authority::name));
  }

  public Account findById(UUID accountId) {
    final String sql = " SELECT * FROM Account WHERE accountId = ? ";
    return jdbc().query(sql, accountMapper, accountId).stream().findAny().orElse(null);
  }

  public Optional<Account> findByName(String name) {
    return jdbc().query(" SELECT * FROM Account WHERE name = lower(?) ", accountMapper, name)
        .stream()
        .findAny();
  }

  public void updateAuthorities(UUID accountId, EnumSet<Authority> authorities) {
    jdbc().update(" UPDATE Account SET authorities = ? WHERE accountId = ? ",
        authoritiesToVarcharArray(authorities),
        accountId);
  }

  public void updatePasswordHash(UUID accountId, String passwordHash) {
    jdbc().update(" UPDATE Account SET passwordHash = ? WHERE accountId = ? ",
        passwordHash,
        accountId);
  }
}
