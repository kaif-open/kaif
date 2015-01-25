/*
 * created on 2005/1/3
 * 
 * $Author$ 
 * $Revision$ 
 * $Date$
 */
package io.kaif.mail;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

import com.google.common.base.Charsets;

@Component
public class JavaMailAgent implements MailAgent {

  static final class EncodingMimeMessagePreparator implements MimeMessagePreparator {

    private final Mail message;

    EncodingMimeMessagePreparator(Mail message) {
      this.message = message;
    }

    public void prepare(MimeMessage mimeMessage) throws MessagingException {
      MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage,
          true,
          Charsets.UTF_8.toString());
      try {
        messageHelper.setFrom(message.getFrom(), message.getFromName());
      } catch (UnsupportedEncodingException e) {
        throw new MessagingException(
            "UnsupportedEncodingException while encode 'the name of FROM person'",
            e);
      }
      messageHelper.setTo(message.getTo());

      if (message.getBcc() != null) {
        messageHelper.setBcc(message.getBcc());
      }

      if (message.getCc() != null) {
        messageHelper.setCc(message.getCc());
      }

      if (message.getReplyTo() != null) {
        messageHelper.setReplyTo(message.getReplyTo());
      }

      messageHelper.setSubject(message.getSubject());
      messageHelper.setText(message.getText());
    }

  }

  private static Logger logger = LoggerFactory.getLogger(JavaMailAgent.class);

  @Autowired
  private JavaMailSender mailSender;

  private ExecutorService executorService = Executors.newFixedThreadPool(5);
  @Autowired
  private MailComposer mailComposer;

  public CompletableFuture<Boolean> send(final Mail message) {
    return CompletableFuture.supplyAsync(() -> {
      //TODO timeout ?
      mailSender.send(new EncodingMimeMessagePreparator(message));
      if (logger.isDebugEnabled()) {
        logger.debug("mail message sent:\n{}", message);
      } else {
        logger.info("mail message sent to:{} ", Arrays.toString(message.getTo()));
      }
      return true;
    }).handle((result, e) -> {
      logger.error("mail message sent failed:"
          + Arrays.toString(message.getTo())
          + ", cause:"
          + e.getMessage());
      return false;
    });
  }

  @Override
  public MailComposer mailComposer() {
    return mailComposer;
  }

  @PreDestroy
  public void destroy() throws Exception {
    executorService.shutdownNow();
    executorService.awaitTermination(5, TimeUnit.SECONDS);
    logger.info("shutdowned");
  }

}