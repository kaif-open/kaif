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

  public static Account create(String username,
      String email,
      String passwordHash,
      Instant createTime) {
    return new Account(UUID.randomUUID(),
        username,
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

  public static boolean isValidUsername(String username) {
    return username != null
        && username.length() >= NAME_MIN
        && username.length() <= NAME_MAX
        && username.matches(NAME_PATTERN);
  }

  private final String username;
  private final UUID accountId;
  private final String email;
  private final String passwordHash;
  private final Instant createTime;
  private final Set<Authority> authorities;

  Account(UUID accountId,
      String username,
      String email,
      String passwordHash,
      Instant createTime,
      Set<Authority> authorities) {
    this.username = username;
    this.accountId = accountId;
    this.email = email;
    this.passwordHash = passwordHash;
    this.createTime = createTime;
    this.authorities = authorities;
  }

  public String getUsername() {
    return username;
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
   * equals and hashCode use `username` instead of accountId for easier testing.
   * username is unique so it is safe
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

    if (username != null ? !username.equals(account.username) : account.username != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return username != null ? username.hashCode() : 0;
  }

  public boolean isActivated() {
    return authorities.contains(Authority.CITIZEN);
  }

  @Override
  public String toString() {
    return "Account{" +
        "username='" + username + '\'' +
        ", accountId=" + accountId +
        ", email='" + email + '\'' +
        ", createTime=" + createTime +
        ", authorities=" + authorities +
        '}';
  }
}
