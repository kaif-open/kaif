package io.kaif.model.exception;

public class OldPasswordNotMatchException extends DomainException {

  @Override
  public String i18nKey() {
    return "account.OldPasswordNotMatchException";
  }
}
