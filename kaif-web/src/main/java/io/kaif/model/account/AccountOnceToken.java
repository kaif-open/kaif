package io.kaif.model.account;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class AccountOnceToken {

  public enum Type {
    ACTIVATION(Duration.ofDays(1)), FORGET_PASSWORD(Duration.ofDays(1)),
    OAUTH_DIRECT_AUTHORIZE(Duration.ofHours(1));

    private final Duration duration;

    private Type(Duration duration) {
      this.duration = duration;
    }

    public Duration getDuration() {
      return duration;
    }
  }

  public static AccountOnceToken create(UUID accountId, Type tokenType, Instant now) {
    return new AccountOnceToken(UUID.randomUUID().toString(), accountId, tokenType, false, now);
  }

  private final String token;
  private final UUID accountId;
  private final Instant createTime;
  private final Type tokenType;
  private final boolean complete;

  AccountOnceToken(String token,
      UUID accountId,
      Type tokenType,
      boolean complete,
      Instant createTime) {
    this.token = token;
    this.accountId = accountId;
    this.createTime = createTime;
    this.tokenType = tokenType;
    this.complete = complete;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AccountOnceToken that = (AccountOnceToken) o;

    if (token != null ? !token.equals(that.token) : that.token != null) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return "AccountOnceToken{" +
        "token='" + token + '\'' +
        ", accountId=" + accountId +
        ", createTime=" + createTime +
        ", type=" + tokenType +
        ", complete=" + complete +
        '}';
  }

  @Override
  public int hashCode() {
    return token != null ? token.hashCode() : 0;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public Type getTokenType() {
    return tokenType;
  }

  public boolean isExpired(Instant now) {
    return now.isAfter(createTime.plus(tokenType.getDuration()));
  }

  public boolean isValid(Instant now) {
    return !isComplete() && !isExpired(now);
  }

  public boolean isComplete() {
    return complete;
  }

  public String getToken() {
    return token;
  }

  Instant getCreateTime() {
    return createTime;
  }
}
