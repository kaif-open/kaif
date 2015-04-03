package io.kaif.model.exception;

public class ClientAppNameReservedException extends DomainException {
  @Override
  public String i18nKey() {
    return "client-app.ClientAppNameReservedException";
  }
}
