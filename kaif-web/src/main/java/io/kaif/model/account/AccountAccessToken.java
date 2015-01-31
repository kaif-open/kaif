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
public class AccountAccessToken {
  public static Optional<AccountAccessToken> tryDecode(String rawToken, AccountSecret secret) {
    List<byte[]> fields = secret.getCodec().tryDecode(rawToken);
    if (fields == null || fields.size() != 3) {
      return Optional.empty();
    }
    final UUID accountId;
    try {
      accountId = Bytes.uuidFromBytes(fields.get(0));
    } catch (RuntimeException e) {
      //malformed UUID, this should not be possible, unless we change protocol
      return Optional.empty();
    }
    return Optional.of(new AccountAccessToken(accountId, fields.get(1), fields.get(2)));
  }

  private static byte[] passwordHashToBytes(String passwordHash) {
    return Bytes.intToBytes(passwordHash.hashCode());
  }

  private static byte[] authoritiesToBytes(Set<Authority> authorities) {
    return Bytes.longToBytes(Authority.toBits(authorities));
  }

  private final UUID accountId;
  private final byte[] authoritiesBits;
  private final byte[] passwordHashDigest;

  public AccountAccessToken(UUID accountId, String passwordHash, Set<Authority> authorities) {
    this(accountId, passwordHashToBytes(passwordHash), authoritiesToBytes(authorities));
  }

  private AccountAccessToken(UUID accountId, byte[] passwordHashDigest, byte[] authoritiesBits) {
    this.accountId = accountId;
    this.passwordHashDigest = passwordHashDigest;
    this.authoritiesBits = authoritiesBits;
  }

  public Set<Authority> getAuthorities() {
    return Authority.fromBits(Bytes.longFromBytes(authoritiesBits));
  }

  public String encode(Instant expireTime, AccountSecret secret) {
    List<byte[]> fields = Arrays.asList(Bytes.uuidToBytes(accountId),
        passwordHashDigest,
        authoritiesBits);
    return secret.getCodec().encode(expireTime.toEpochMilli(), fields);
  }

  /**
   * matches() only check if any data changed, it has no security meaning. the actual protection is
   * base on SecureTokenCodec, not this method.
   */
  public boolean matches(String passwordHash, Set<Authority> authorities) {
    return Arrays.equals(this.passwordHashDigest, passwordHashToBytes(passwordHash))
        && Arrays.equals(this.authoritiesBits, authoritiesToBytes(authorities));
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

    if (!Arrays.equals(authoritiesBits, that.authoritiesBits)) {
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
    int result = passwordHashDigest != null ? Arrays.hashCode(passwordHashDigest) : 0;
    result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
    result = 31 * result + (authoritiesBits != null ? Arrays.hashCode(authoritiesBits) : 0);
    return result;
  }

  @Override
  public String toString() {
    return "AccountAccessToken{" +
        "accountId='" + accountId + '\'' +
        '}';
  }

  public UUID getAccountId() {
    return accountId;
  }

}
