package io.kaif.model.exception;

public class AuthenticateFailException extends DomainException {

  @Override
  public String i18nKey() {
    return "account.AuthenticateFailException";
  }
}
