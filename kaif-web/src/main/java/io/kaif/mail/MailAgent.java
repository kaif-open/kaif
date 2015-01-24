/*
 * created on 2005/1/3
 * 
 * $Author$
 * $Revision$ 
 * $Date$
 */
package io.kaif.mail;

import java.util.concurrent.CompletableFuture;

import org.springframework.mail.MailException;

/**
 * @author ingramchen
 */
public interface MailAgent {

  CompletableFuture<Boolean> send(Mail mailMessage) throws MailException;

  MailComposer mailComposer();
}