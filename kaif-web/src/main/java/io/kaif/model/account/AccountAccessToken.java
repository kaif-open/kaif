package io.kaif.model.account;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import io.kaif.token.Bytes;

/**
 * since token are transfer to client side, so to make it shorter, we only use hashCode() of
 * passwordHash and authorities. the current size is around 115 bytes
 * <p>
 * this also prevent leak detail passwordHash to client side
 */
public class AccountAccessToken implements Authorization {

  public static final String HEADER_KEY = "X-KAIF-ACCESS-TOKEN";

  public static Optional<AccountAccessToken> tryDecode(String rawToken, AccountSecret secret) {
    List<byte[]> fields = secret.getCodec().tryDecode(rawToken);
    if (fields == null || fields.size() != 3) {
      return Optional.empty();
    }
    final UUID accountId;
    final long authoritiesBits;
    try {
      accountId = Bytes.uuidFromBytes(fields.get(0));
      authoritiesBits = Bytes.longFromBytes(fields.get(2));
    } catch (RuntimeException e) {
      //malformed UUID or bits, this should not be possible, unless we change protocol
      return Optional.empty();
    }
    return Optional.of(new AccountAccessToken(accountId, fields.get(1), authoritiesBits));
  }

  private static byte[] passwordHashToBytes(String passwordHash) {
    return Bytes.intToBytes(passwordHash.hashCode());
  }

  private final UUID accountId;
  private final long authoritiesBits;
  private final byte[] passwordHashDigest;

  public AccountAccessToken(UUID accountId, String passwordHash, Set<Authority> authorities) {
    this(accountId, passwordHashToBytes(passwordHash), Authority.toBits(authorities));
  }

  private AccountAccessToken(UUID accountId, byte[] passwordHashDigest, long authoritiesBits) {
    this.accountId = accountId;
    this.passwordHashDigest = passwordHashDigest;
    this.authoritiesBits = authoritiesBits;
  }

  public String encode(Instant expireTime, AccountSecret secret) {
    List<byte[]> fields = Arrays.asList(Bytes.uuidToBytes(accountId),
        passwordHashDigest,
        Bytes.longToBytes(authoritiesBits));
    return secret.getCodec().encode(expireTime.toEpochMilli(), fields);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AccountAccessToken that = (AccountAccessToken) o;

    if (authoritiesBits != that.authoritiesBits) {
      return false;
    }
    if (accountId != null ? !accountId.equals(that.accountId) : that.accountId != null) {
      return false;
    }
    if (!Arrays.equals(passwordHashDigest, that.passwordHashDigest)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = accountId != null ? accountId.hashCode() : 0;
    result = 31 * result + (int) (authoritiesBits ^ (authoritiesBits >>> 32));
    result = 31 * result + (passwordHashDigest != null ? Arrays.hashCode(passwordHashDigest) : 0);
    return result;
  }

  @Override
  public String toString() {
    return "AccountAccessToken{" +
        "accountId='" + accountId + '\'' +
        '}';
  }

  @Override
  public UUID authenticatedId() {
    return accountId;
  }

  @Override
  public boolean containsAuthority(Authority authority) {
    return Authority.bitsContains(authoritiesBits, authority);
  }

  @Override
  public boolean matches(Account account) {
    return authenticatedId().equals(account.getAccountId())
        && Arrays.equals(this.passwordHashDigest, passwordHashToBytes(account.getPasswordHash()))
        && this.authoritiesBits == Authority.toBits(account.getAuthorities());
  }
}
