package io.kaif.oauth;

import io.kaif.model.clientapp.ClientAppScope;

public class InsufficientScopeException extends OauthException {

  private final ClientAppScope requiredScope;

  public InsufficientScopeException(ClientAppScope requiredScope) {
    this.requiredScope = requiredScope;
  }

  public ClientAppScope getRequiredScope() {
    return requiredScope;
  }
}
