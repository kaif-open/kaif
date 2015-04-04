package io.kaif.model.exception;

public class RequireCitizenException extends DomainException {

  @Override
  public String i18nKey() {
    return "account.RequireCitizenException";
  }
}
