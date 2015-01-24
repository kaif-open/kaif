package io.kaif.mail;

import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mail.MailException;

import io.kaif.model.account.Account;

public class MailAgentTest extends MailTestCase {

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
}