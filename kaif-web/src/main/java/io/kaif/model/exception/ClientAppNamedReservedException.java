package io.kaif.model.exception;

public class ClientAppNamedReservedException extends DomainException {
  @Override
  public String i18nKey() {
    return "client-app.ClientAppNamedReservedException";
  }
}
