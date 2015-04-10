package io.kaif.model.clientapp;

import java.util.UUID;

import javax.annotation.Nullable;

public interface ClientAppAuthorization {

  UUID authenticatedId();

  String clientId();

  boolean validate(@Nullable ClientAppUser clientAppUser);

  boolean containsScope(ClientAppScope scope);
}
