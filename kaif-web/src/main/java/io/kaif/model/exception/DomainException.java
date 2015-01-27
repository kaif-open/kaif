package io.kaif.model.exception;

/**
 * DomainException will be translated to i18n on RestExceptionHandler, so dart can just display
 * toString() of error
 */
public abstract class DomainException extends RuntimeException implements I18nAware {
}
