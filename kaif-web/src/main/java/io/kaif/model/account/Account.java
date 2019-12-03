package io.kaif.model.account;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.concurrent.Immutable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import io.kaif.kmark.KmarkProcessor;

@Immutable
public class Account implements Authorization {
  public static final int PASSWORD_MIN = 6;
  public static final int PASSWORD_MAX = 100;
  public static final int NAME_MIN = 3;
  public static final int NAME_MAX = 15;

  //changing pattern should review sign_up_form.dart, Emitter.java
  public static final String NAME_PATTERN = "^[a-zA-Z_0-9]{3,15}$";

  public static Account create(String username,
      String email,
      String passwordHash,
      Instant createTime) {
    return new Account(UUID.randomUUID(),
        username,
        email,
        passwordHash,
        "",
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
        && username.matches(NAME_PATTERN)
        && !username.equalsIgnoreCase("null");
  }

  public static String renderDescriptionPreview(String description) {
    return KmarkProcessor.process(description);
  }

  private final String username;
  private final UUID accountId;
  private final String email;
  private final String passwordHash;
  private final String description;
  private final Instant createTime;
  private final Set<Authority> authorities;

  Account(UUID accountId,
      String username,
      String email,
      String passwordHash,
      String description,
      Instant createTime,
      Set<Authority> authorities) {
    this.username = username;
    this.accountId = accountId;
    this.description = description;
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

  /**
   * do not include password hash in toString
   */

  public Set<Authority> getAuthorities() {
    return authorities;
  }

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
    return "Account{"
        + "username='"
        + username
        + '\''
        + ", accountId="
        + accountId
        + ", email='"
        + email
        + '\''
        + ", description='"
        + description
        + '\''
        + ", createTime="
        + createTime
        + ", authorities="
        + authorities
        + '}';
  }

  @Override
  public UUID authenticatedId() {
    return accountId;
  }

  @Override
  public boolean containsAuthority(Authority authority) {
    return authorities.contains(authority);
  }

  @Override
  public boolean matches(Account account) {
    return authenticatedId().equals(account.getAccountId())
        && passwordHash.equals(account.getPasswordHash())
        && authorities.equals(account.getAuthorities());
  }

  public Account withDescription(String description) {
    Preconditions.checkNotNull(KmarkProcessor.process(description));
    return new Account(accountId,
        username,
        email,
        passwordHash,
        description,
        createTime,
        authorities);
  }

  public Account withAuthorities(Set<Authority> authorities) {
    Preconditions.checkArgument(!authorities.contains(Authority.FORBIDDEN));
    return new Account(accountId,
        username,
        email,
        passwordHash,
        description,
        createTime,
        authorities);
  }

  public String getRenderDescription() {
    //username as anchor prefix
    return KmarkProcessor.process(description);
  }

  public String getDescription() {
    return description;
  }

  public String getEscapedDescription() {
    return KmarkProcessor.escapeHtml(description);
  }

  @VisibleForTesting
  public Account withPasswordHash(String newPasswordHash) {
    return new Account(accountId,
        username,
        email,
        newPasswordHash,
        description,
        createTime,
        authorities);
  }
}
