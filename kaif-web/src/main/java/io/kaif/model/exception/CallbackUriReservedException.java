package io.kaif.model.exception;

public class CallbackUriReservedException extends DomainException {
  @Override
  public String i18nKey() {
    return "client-app.CallbackUriReservedException";
  }
}
