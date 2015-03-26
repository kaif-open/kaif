package io.kaif.model.exception;

public class CreditNotEnoughException extends DomainException {

  @Override
  public String i18nKey() {
    return "zone.CreditNotEnoughException";
  }
}
