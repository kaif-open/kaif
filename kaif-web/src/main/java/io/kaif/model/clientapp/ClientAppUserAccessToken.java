package io.kaif.model.clientapp;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Charsets;

import io.kaif.model.account.Account;
import io.kaif.model.account.Authority;
import io.kaif.model.account.Authorization;
import io.kaif.token.Bytes;

/**
 * requirement:
 * <ul>
 * <li>support multiple tokens (different scopes for different devices) at the same time</li>
 * <li>invalid if user revoke the client app</li>
 * <li>invalid if client app revoked</li>
 * <li>invalid if client app reset secret</li>
 * <li>account's authorities changed should treat invalid (hard to implement)</li>
 * <li>account password changed do not affect this token</li>
 * </ul>
 */
public class ClientAppUserAccessToken implements Authorization, ClientAppAuthorization {

  public static Optional<ClientAppUserAccessToken> tryDecode(String rawToken, OauthSecret secret) {
    List<byte[]> fields = secret.getCodec().tryDecode(rawToken);
    if (fields == null || fields.size() != 5) {
      return Optional.empty();
    }
    try {
      return Optional.of(new ClientAppUserAccessToken(//
          Bytes.uuidFromBytes(fields.get(0)),
          Bytes.longFromBytes(fields.get(1)),
          ClientAppScope.tryParse(new String(fields.get(2), Charsets.UTF_8)),
          new String(fields.get(3), Charsets.UTF_8),
          new String(fields.get(4), Charsets.UTF_8)));
    } catch (RuntimeException e) {
      return Optional.empty();
    }
  }

  private final long authoritiesBits;
  // validate accountId+clientId against ClientAppUser so we can detect if user revoked the
  // client app
  private final UUID accountId;
  private final String clientId;
  // validate clientSecret in database so we can detect client app revoked or reset client secret
  private final String clientSecret;
  // tokenScopes is only for current tokens, this may not the same as ClientAppUser's
  // lastGrantedScopes because multiple devices may issue different scopes
  // (different access token), but ClientAppUser only store latest issued scopes
  private final Set<ClientAppScope> tokenScopes;

  public ClientAppUserAccessToken(UUID accountId,
      Set<Authority> authorities,
      Set<ClientAppScope> tokenScopes,
      String clientId,
      String clientSecret) {
    this(accountId, Authority.toBits(authorities), tokenScopes, clientId, clientSecret);
  }

  private ClientAppUserAccessToken(UUID accountId,
      long authoritiesBits,
      Set<ClientAppScope> tokenScopes,
      String clientId,
      String clientSecret) {
    this.authoritiesBits = authoritiesBits;
    this.accountId = accountId;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.tokenScopes = tokenScopes;
  }

  @Override
  public String toString() {
    return "ClientAppUserAccessToken{" +
        "authoritiesBits=" + authoritiesBits +
        ", accountId=" + accountId +
        ", clientId='" + clientId + '\'' +
        ", clientSecret='" + clientSecret + '\'' +
        ", tokenScopes=" + tokenScopes +
        '}';
  }

  public String encode(Instant expireTime, OauthSecret secret) {
    List<byte[]> fields = Arrays.asList(Bytes.uuidToBytes(accountId),
        Bytes.longToBytes(authoritiesBits),
        ClientAppScope.toCanonicalString(tokenScopes).getBytes(Charsets.UTF_8),
        clientId.getBytes(Charsets.UTF_8),
        clientSecret.getBytes(Charsets.UTF_8));
    return secret.getCodec().encode(expireTime.toEpochMilli(), fields);
  }

  @Override
  public UUID authenticatedId() {
    return accountId;
  }

  @Override
  public boolean containsAuthority(Authority authority) {
    // if user change authority (for example, ban by sysop), the token is treat invalid
    return Authority.bitsContains(authoritiesBits, authority);
  }

  @Override
  public boolean matches(Account account) {
    // oauth access token do not honor account's password to revoke token.
    // because it rely on revoke of ClientAppUser
    return authenticatedId().equals(account.getAccountId())
        && this.authoritiesBits == Authority.toBits(account.getAuthorities());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ClientAppUserAccessToken that = (ClientAppUserAccessToken) o;

    if (authoritiesBits != that.authoritiesBits) {
      return false;
    }
    if (accountId != null ? !accountId.equals(that.accountId) : that.accountId != null) {
      return false;
    }
    if (clientId != null ? !clientId.equals(that.clientId) : that.clientId != null) {
      return false;
    }
    if (clientSecret != null
        ? !clientSecret.equals(that.clientSecret)
        : that.clientSecret != null) {
      return false;
    }
    return !(tokenScopes != null
             ? !tokenScopes.equals(that.tokenScopes)
             : that.tokenScopes != null);

  }

  @Override
  public int hashCode() {
    int result = (int) (authoritiesBits ^ (authoritiesBits >>> 32));
    result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
    result = 31 * result + (clientId != null ? clientId.hashCode() : 0);
    result = 31 * result + (clientSecret != null ? clientSecret.hashCode() : 0);
    result = 31 * result + (tokenScopes != null ? tokenScopes.hashCode() : 0);
    return result;
  }

  /**
   * validate app revoked or reset secret or user revoked
   * <p>
   * ClientAppUser may be deleted (revoked) so we accept null value here.
   */
  @Override
  public boolean validate(@Nullable ClientAppUser clientAppUser) {
    return Optional.ofNullable(clientAppUser)
        .filter(user -> authenticatedId().equals(user.getAccountId()))
        .filter(user -> clientId().equals(user.getClientId()))
        .filter(user -> clientSecret.equals(user.getCurrentClientSecret()))
        .isPresent();
  }

  /**
   * validate granted scope
   */
  @Override
  public boolean containsScope(ClientAppScope scope) {
    return tokenScopes.contains(scope);
  }

  @Override
  public String clientId() {
    return clientId;
  }
}
