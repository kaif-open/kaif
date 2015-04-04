package io.kaif.model.clientapp;

import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.google.common.base.Charsets;

import io.kaif.token.Bytes;

public class GrantCode {

  public static Optional<GrantCode> tryDecode(String rawToken, OauthSecret secret) {
    List<byte[]> fields = secret.getCodec().tryDecode(rawToken);
    if (fields == null || fields.size() != 5) {
      return Optional.empty();
    }
    try {
      return Optional.of(new GrantCode(//
          Bytes.uuidFromBytes(fields.get(0)),
          new String(fields.get(1), Charsets.UTF_8),
          new String(fields.get(2), Charsets.UTF_8),
          new String(fields.get(3), Charsets.UTF_8),
          ClientAppScope.tryParse(new String(fields.get(4), Charsets.UTF_8))));
    } catch (RuntimeException e) {
      return Optional.empty();
    }
  }

  private final UUID accountId;
  private final String clientId;
  private final String clientSecret;
  private final String redirectUri;
  private final Set<ClientAppScope> scopes;

  public GrantCode(UUID accountId,
      String clientId,
      String clientSecret,
      String redirectUri,
      Set<ClientAppScope> scopes) {
    this.accountId = accountId;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.redirectUri = redirectUri;
    this.scopes = EnumSet.copyOf(scopes);
  }

  public String encode(Instant expireTime, OauthSecret secret) {
    List<byte[]> fields = Arrays.asList(Bytes.uuidToBytes(accountId),
        clientId.getBytes(Charsets.UTF_8),
        clientSecret.getBytes(Charsets.UTF_8),
        redirectUri.getBytes(Charsets.UTF_8),
        getCanonicalScope().getBytes(Charsets.UTF_8));
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

    GrantCode grantCode = (GrantCode) o;

    if (accountId != null ? !accountId.equals(grantCode.accountId) : grantCode.accountId != null) {
      return false;
    }
    if (clientId != null ? !clientId.equals(grantCode.clientId) : grantCode.clientId != null) {
      return false;
    }
    if (clientSecret != null
        ? !clientSecret.equals(grantCode.clientSecret)
        : grantCode.clientSecret != null) {
      return false;
    }
    if (redirectUri != null
        ? !redirectUri.equals(grantCode.redirectUri)
        : grantCode.redirectUri != null) {
      return false;
    }
    return !(scopes != null ? !scopes.equals(grantCode.scopes) : grantCode.scopes != null);

  }

  @Override
  public int hashCode() {
    int result = accountId != null ? accountId.hashCode() : 0;
    result = 31 * result + (clientId != null ? clientId.hashCode() : 0);
    result = 31 * result + (clientSecret != null ? clientSecret.hashCode() : 0);
    result = 31 * result + (redirectUri != null ? redirectUri.hashCode() : 0);
    result = 31 * result + (scopes != null ? scopes.hashCode() : 0);
    return result;
  }

  public boolean matches(ClientApp clientApp, String targetRedirectUri) {
    return redirectUri.equals(targetRedirectUri)
        && clientId.equals(clientApp.getClientId())
        && clientSecret.equals(clientApp.getClientSecret());
  }

  public String getCanonicalScope() {
    return ClientAppScope.toCanonicalString(scopes);
  }
}
