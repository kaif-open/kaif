/*
 * created on 2005/1/3
 * 
 * $Author$
 * $Revision$ 
 * $Date$
 */
package io.kaif.mail;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import org.springframework.mail.MailException;

import com.google.common.collect.ImmutableMap;

import io.kaif.model.account.Account;

/**
 * @author ingramchen
 */
public interface MailAgent {

  CompletableFuture<Boolean> send(Mail mailMessage) throws MailException;

  MailComposer mailComposer();

  default CompletableFuture<Boolean> sendAccountActivation(Locale locale,
      Account account,
      String activationId) {

    Mail mail = mailComposer().createMail();
    mail.setTo(account.getEmail());
    mail.setSubject(mailComposer().i18n(locale, "email.activation.title"));
    String body = mailComposer().compose(locale,
        "/account-activation.ftl",
        ImmutableMap.of("account", account, "activationId", activationId));
    mail.setText(body);
    return send(mail);
  }

  default CompletableFuture<Boolean> sendResetPassword(Locale locale, Account account, String onceToken) {

    Mail mail = mailComposer().createMail();
    mail.setTo(account.getEmail());
    mail.setSubject(mailComposer().i18n(locale, "email.reset_password.title"));
    String body = mailComposer().compose(locale,
        "/account-reset-password.ftl",
        ImmutableMap.of("account", account, "token", onceToken));
    mail.setText(body);
    return send(mail);
  }
}