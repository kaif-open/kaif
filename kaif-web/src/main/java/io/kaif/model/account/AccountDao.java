package io.kaif.model.account;

import java.sql.Array;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import io.kaif.database.DaoOperations;

@Repository
public class AccountDao implements DaoOperations {

  private final RowMapper<Account> accountMapper = (rs, rowNum) -> {
    Set<Authority> authorities = convertVarcharArray(rs.getArray("authorities")).map(Authority::valueOf)
        .collect(Collectors.toSet());
    return new Account(//
        UUID.fromString(rs.getString("accountId")),
        rs.getString("username"),
        rs.getString("email"),
        rs.getString("passwordHash"),
        rs.getTimestamp("createTime").toInstant(),
        authorities);
  };

  private final RowMapper<AccountOnceToken> tokenMapper = (rs, rowNum) -> {
    AccountOnceToken.Type tokenType = AccountOnceToken.Type.valueOf(rs.getString("tokenType"));
    return new AccountOnceToken(//
        rs.getString("token"),
        UUID.fromString(rs.getString("accountId")),
        tokenType,
        rs.getBoolean("complete"),
        rs.getTimestamp("createTime").toInstant());
  };

  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Override
  public NamedParameterJdbcTemplate namedJdbc() {
    return namedParameterJdbcTemplate;
  }

  public Account create(String username, String email, String passwordHash, Instant now) {

    final Account account = Account.create(username, email, passwordHash, now);
    Preconditions.checkArgument(!account.getAuthorities().contains(Authority.FORBIDDEN));
    jdbc().update(""
            + " INSERT "
            + "   INTO Account "
            + "        (accountId, email, passwordHash, username, "
            + "         createTime, authorities) "
            + " VALUES "
            + questions(6),
        account.getAccountId(),
        account.getEmail().toLowerCase(),
        account.getPasswordHash(),
        account.getUsername(),
        Timestamp.from(account.getCreateTime()),
        authoritiesToVarcharArray(account.getAuthorities()));

    return account;
  }

  private Array authoritiesToVarcharArray(Set<Authority> authorities) {
    return createVarcharArray(authorities.stream().map(Authority::name));
  }

  public Optional<Account> findById(UUID accountId) {
    final String sql = " SELECT * FROM Account WHERE accountId = ? LIMIT 1 ";
    return jdbc().query(sql, accountMapper, accountId).stream().findAny();
  }

  public Optional<Account> findByUsername(String username) {
    return jdbc().query(" SELECT * FROM Account WHERE username = lower(?) LIMIT 1 ",
        accountMapper,
        username).stream().findAny();
  }

  public void updateAuthorities(UUID accountId, EnumSet<Authority> authorities) {
    Preconditions.checkArgument(!authorities.contains(Authority.FORBIDDEN));
    jdbc().update(" UPDATE Account SET authorities = ? WHERE accountId = ? ",
        authoritiesToVarcharArray(authorities),
        accountId);
  }

  public void updatePasswordHash(UUID accountId, String passwordHash) {
    jdbc().update(" UPDATE Account SET passwordHash = ? WHERE accountId = ? ",
        passwordHash,
        accountId);
  }

  public boolean isEmailAvailable(String email) {
    final String sql = " SELECT count(*) FROM Account WHERE email = ? LIMIT 1 ";
    return jdbc().queryForObject(sql, Number.class, email.toLowerCase()).intValue() == 0;
  }

  @VisibleForTesting
  public List<AccountOnceToken> listOnceTokens() {
    final String sql = " SELECT * FROM AccountOnceToken ";
    return jdbc().query(sql, tokenMapper);
  }

  public AccountOnceToken createOnceToken(Account account,
      AccountOnceToken.Type tokenType,
      Instant now) {
    AccountOnceToken onceToken = AccountOnceToken.create(account.getAccountId(), tokenType, now);
    jdbc().update(""
            + " INSERT "
            + "   INTO AccountOnceToken "
            + "        (token, accountId, tokenType, "
            + "         complete, createTime ) "
            + " VALUES "
            + questions(5),
        onceToken.getToken(),
        onceToken.getAccountId(),
        onceToken.getTokenType().name(),
        onceToken.isComplete(),
        Timestamp.from(onceToken.getCreateTime()));
    return onceToken;
  }

  public Optional<AccountOnceToken> findOnceToken(String token, AccountOnceToken.Type tokenType) {
    final String sql = " SELECT * FROM AccountOnceToken WHERE token = ? AND tokenType = ? LIMIT 1 ";
    return jdbc().query(sql, tokenMapper, token, tokenType.name()).stream().findFirst();
  }

  public void completeOnceToken(AccountOnceToken onceToken) {
    jdbc().update(" UPDATE AccountOnceToken SET complete = ? WHERE token = ? ",
        true,
        onceToken.getToken());
  }
}
