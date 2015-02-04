package io.kaif.mail;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mail.MailException;

import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import io.kaif.model.account.Account;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MailAgentTest extends MailTestCases {

  private MailAgent mockMailAgent;
  private MailAgent mailAgent;

  @Before
  public void setUp() throws Exception {

    MailProperties mailProperties = new MailProperties();
    mailProperties.setSenderName("Kaif");
    mailProperties.setSenderAddress("noreply@kaif.io");

    MailComposer mailComposer = new MailComposer(messageSource, configuration, mailProperties);
    mockMailAgent = Mockito.mock(MailAgent.class);
    when(mockMailAgent.mailComposer()).thenReturn(mailComposer);
    mailAgent = new MailAgent() {

      @Override
      public CompletableFuture<Boolean> send(Mail mailMessage) throws MailException {
        return mockMailAgent.send(mailMessage);
      }

      @Override
      public MailComposer mailComposer() {
        return mockMailAgent.mailComposer();
      }
    };
  }

  @Test
  public void sendAccountActivation() throws Exception {
    Account account = Account.create("aName", "foo@gmail.com", "pw", Instant.now());
    mailAgent.sendAccountActivation(Locale.ENGLISH, account, "myActivationId");
    Mail mailMessage = new Mail();
    mailMessage.setFrom("noreply@kaif.io");
    mailMessage.setSubject("kaif account activation");
    mailMessage.setText("Dear aName\n"
        + "\n"
        + "Your kaif account has been created, please click on the URL below to activate it:\n"
        + "\n"
        + "  http://kaif.io/account/activation?key=myActivationId\n"
        + "\n"
        + "Regards,\n"
        + "\n"
        + "- kaif Team.");
    mailMessage.setTo("foo@gmail.com");
    verify(mockMailAgent).send(mailMessage);
  }

  @Test
  public void sendResetPassword() throws Exception {
    Account account = Account.create("aName", "foo@gmail.com", "pw", Instant.now());
    mailAgent.sendResetPassword(Locale.ENGLISH, account, "myToken");
    Mail mailMessage = new Mail();
    mailMessage.setFrom("noreply@kaif.io");
    mailMessage.setSubject("kaif account password reset");
    mailMessage.setText("Dear aName\n"
        + "\n"
        + "You have been requested password reset, please click on the URL below to reset it:\n"
        + "\n"
        + "  http://kaif.io/account/reset-password?key=myToken\n"
        + "\n"
        + "Regards,\n"
        + "\n"
        + "- kaif Team.");
    mailMessage.setTo("foo@gmail.com");
    verify(mockMailAgent).send(mailMessage);
  }

  @Test
  public void sendPasswordWasReset() throws Exception {
    Account account = Account.create("aName", "foo@gmail.com", "pw", Instant.now());
    mailAgent.sendPasswordWasReset(Locale.ENGLISH, account);
    Mail mailMessage = new Mail();
    mailMessage.setFrom("noreply@kaif.io");
    mailMessage.setSubject("Your kaif password has been reset.");
    mailMessage.setText("Dear aName\n"
        + "\n"
        + "The password for your kaif id, has been successfully reset.\n"
        + "\n"
        + "If you didn’t make this change or if you believe an unauthorized person has accessed your account, go to http://kaif.io/account/forget-password to reset your password immediately.\n"
        + "\n"
        + "Regards,\n"
        + "\n"
        + "- kaif Team.");
    mailMessage.setTo("foo@gmail.com");
    verify(mockMailAgent).send(mailMessage);
  }
}