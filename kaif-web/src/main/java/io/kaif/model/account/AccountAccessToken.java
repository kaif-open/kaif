package io.kaif.model.account;

import static java.util.stream.Collectors.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountAccessToken {
  public static Optional<AccountAccessToken> tryDecode(String rawToken, AccountSecret secret) {
    List<String> fields = secret.getCodec().tryDecodeAsString(rawToken);
    if (fields == null || fields.size() != 3) {
      return Optional.empty();
    }
    Set<Authority> auths = Pattern.compile(",")
        .splitAsStream(fields.get(2))
        .map(Authority::valueOf)
        .collect(toSet());
    return Optional.of(new AccountAccessToken(fields.get(0), fields.get(1), auths));
  }
  private final String passwordHash;
  private final String name;
  private final Set<Authority> authorities;

  public AccountAccessToken(String passwordHash, String name, Set<Authority> authorities) {
    this.passwordHash = passwordHash;
    this.name = name;
    this.authorities = authorities;
  }

  public String encode(Duration expire, AccountSecret secret) {
    List<byte[]> fields = Stream.of(passwordHash,
        name,
        authorities.stream().map(Authority::name).collect(Collectors.joining(",")))
        .map(String::getBytes)
        .collect(toList());
    return secret.getCodec().encode(Instant.now().plus(expire).toEpochMilli(), fields);
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

    if (authorities != null ? !authorities.equals(that.authorities) : that.authorities != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (passwordHash != null
        ? !passwordHash.equals(that.passwordHash)
        : that.passwordHash != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = passwordHash != null ? passwordHash.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (authorities != null ? authorities.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "AccountAccessToken{" +
        "name='" + name + '\'' +
        ", authorities=" + authorities +
        '}';
  }

  public String getName() {
    return name;
  }

  public String getPasswordHash() {
    return passwordHash;
  }
}
