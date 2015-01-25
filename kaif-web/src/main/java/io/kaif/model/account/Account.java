package io.kaif.model.account;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.concurrent.Immutable;

@Immutable
public class Account {
  public static final int PASSWORD_MIN = 6;
  public static final int PASSWORD_MAX = 100;
  public static final int NAME_MIN = 3;
  public static final int NAME_MAX = 15;

  //changing pattern should review sign_up_form.dart
  public static final String NAME_PATTERN = "^[a-zA-Z_0-9]{3,15}$";

  public static Account create(String name, String email, String passwordHash, Instant createTime) {
    return new Account(UUID.randomUUID(),
        name,
        email,
        passwordHash,
        createTime,
        EnumSet.of(Authority.TOURIST));
  }

  public static boolean isValidPassword(String password) {
    return password != null
        && password.length() >= PASSWORD_MIN
        && password.length() <= PASSWORD_MAX;
  }

  public static boolean isValidName(String name) {
    return name != null && name.length() >= NAME_MIN && name.length() <= NAME_MAX && name.matches(
        NAME_PATTERN);
  }

  private final String name;
  private final UUID accountId;
  private final String email;
  private final String passwordHash;
  private final Instant createTime;
  private final Set<Authority> authorities;

  Account(UUID accountId,
      String name,
      String email,
      String passwordHash,
      Instant createTime,
      Set<Authority> authorities) {
    this.name = name;
    this.accountId = accountId;
    this.email = email;
    this.passwordHash = passwordHash;
    this.createTime = createTime;
    this.authorities = authorities;
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
    return authorities.contains(Authority.CITIZEN);
  }

  @Override
  public String toString() {
    return "Account{" +
        "name='" + name + '\'' +
        ", accountId=" + accountId +
        ", email='" + email + '\'' +
        ", createTime=" + createTime +
        ", authorities=" + authorities +
        '}';
  }
}
