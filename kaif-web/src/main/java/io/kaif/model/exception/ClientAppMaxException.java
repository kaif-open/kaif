package io.kaif.model.exception;

import java.util.Collections;
import java.util.List;

public class ClientAppMaxException extends DomainException {
  private final int maxNoOfApps;

  public ClientAppMaxException(int maxNoOfApps) {
    this.maxNoOfApps = maxNoOfApps;
  }

  @Override
  public String i18nKey() {
    return "client-app.ClientAppMaxException";
  }

  @Override
  public List<String> i18nArgs() {
    return Collections.singletonList(String.valueOf(maxNoOfApps));
  }
}
