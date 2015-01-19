package io.kaif.model.account;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Account {
  public static Account create(String name, String email, String passwordHash, Instant createTime) {
    return new Account(UUID.randomUUID(),
        name,
        email,
        passwordHash,
        createTime,
        EnumSet.of(Authority.NORMAL),
        false);
  }

  public static boolean isValidPassword(String password) {
    return password != null && password.length() >= 6;
  }
  private final String name;
  private final UUID accountId;
  private final String email;
  private final String passwordHash;
  private final Instant createTime;
  private final Set<Authority> authorities;
  private final boolean activated;

  Account(UUID accountId,
      String name,
      String email,
      String passwordHash,
      Instant createTime,
      Set<Authority> authorities,
      boolean activated) {
    this.name = name;
    this.accountId = accountId;
    this.email = email;
    this.passwordHash = passwordHash;
    this.createTime = createTime;
    this.authorities = authorities;
    this.activated = activated;
  }

  public String getName() {
    return name;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public Instant getCreateTime() {
    return createTime;
  }

  public Set<Authority> getAuthorities() {
    return authorities;
  }

  /**
   * do not include password hash in toString
   */
  @Override
  public String toString() {
    return "Account{" +
        "name='" + name + '\'' +
        ", accountId=" + accountId +
        ", email='" + email + '\'' +
        ", createTime=" + createTime +
        ", authorities=" + authorities +
        ", activated=" + activated +
        '}';
  }

  /**
   * equals and hashCode use `name` instead of accountId for easier testing.
   * Name is unique so it is safe
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Account account = (Account) o;

    if (name != null ? !name.equals(account.name) : account.name != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }

  public boolean isActivated() {
    return activated;
  }
}
