package io.kaif.mail;

import static java.util.Arrays.asList;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.google.common.base.Charsets;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

// temporary disable because we don't have aws ses yet
// @Component
public class AwsSesMailAgent implements MailAgent {

  private static final Logger logger = LoggerFactory.getLogger(AwsSesMailAgent.class);

  private AmazonSimpleEmailServiceClient client;
  private final ExecutorService executor = Executors.newFixedThreadPool(5,
      new ThreadFactoryBuilder().setNameFormat("aws-ses-mail-agent-pool-%d").build());

  @Autowired
  private MailProperties mailProperties;

  @Autowired
  private MailComposer mailComposer;
  //TODO adjust rate limiter if our aws ses mail passed
  private final RateLimiter rateLimiter = RateLimiter.create(5.0);

  @Override
  public MailComposer mailComposer() {
    return mailComposer;
  }

  @PostConstruct
  public void afterPropertiesSet() {
    AWSCredentials awsSesCredentials = new BasicAWSCredentials(mailProperties.getAwsAccessKey(),
        mailProperties.getAwsSecretKey());
    this.client = new AmazonSimpleEmailServiceClient(awsSesCredentials);
    logger.info("mail agent ready, sender:"
        + mailProperties.getAwsSenderAddress()
        + ", access key:"
        + awsSesCredentials.getAWSAccessKeyId());
  }

  @PreDestroy
  public void destroy() throws Exception {
    logger.info("shutdowning, wait all mails sent...");
    executor.shutdown();
    try {
      executor.awaitTermination(1, TimeUnit.MINUTES);
    } finally {
      this.client.shutdown();
    }
    logger.info("shutdowned.");
  }

  @Override
  public CompletableFuture<Boolean> send(Mail mailMessage) throws MailException {
    Message message = new Message();
    message.setSubject(new Content(mailMessage.getSubject()).withCharset(Charsets.UTF_8.toString()));
    message.setBody(new Body(new Content(mailMessage.getText()).withCharset(Charsets.UTF_8.toString())));

    Destination destination = new Destination(asList(mailMessage.getTo()));

    Optional.ofNullable(mailMessage.getCc())
        .filter(cc -> cc.length > 0)
        .ifPresent(cc -> destination.setCcAddresses(asList(cc)));

    Optional.ofNullable(mailMessage.getBcc())
        .filter(cc -> cc.length > 0)
        .ifPresent(cc -> destination.setBccAddresses(asList(cc)));

    SendEmailRequest sendEmailRequest = new SendEmailRequest(composeSource(mailMessage).toString(),
        destination,
        message);

    Optional.ofNullable(mailMessage.getReplyTo())
        .ifPresent(r -> sendEmailRequest.setReplyToAddresses(asList(r)));

    return CompletableFuture.supplyAsync(() -> {
      double totalWait = rateLimiter.acquire();
      if (totalWait > 5) {
        logger.warn("rate limit wait too long: " + totalWait + " seconds");
      }
      SendEmailResult emailResult = client.sendEmail(sendEmailRequest);
      if (logger.isDebugEnabled()) {
        logger.debug("sent mail messageId:{}, body:\n{}", emailResult.getMessageId(), mailMessage);
      } else {
        logger.info("sent mail to {}, messageId:{}", destination, emailResult.getMessageId());
      }
      return true;
    }, executor).handle((result, e) -> {
      if (e != null) {
        logger.warn("fail send mail to " + destination + ", error:" + e.getMessage());
        return false;
      }
      return true;
    });
  }

  /*
   * check spring MimeMessageHelper for how to encode RFC4072
   */
  private InternetAddress composeSource(Mail mailMessage) {
    try {
      if (mailMessage.getFromName() == null) {
        return new InternetAddress(mailProperties.getAwsSenderAddress());
      } else {
        return new InternetAddress(mailProperties.getAwsSenderAddress(),
            mailMessage.getFromName(),
            Charsets.UTF_8.toString());
      }
    } catch (AddressException | UnsupportedEncodingException e) {
      throw new IllegalArgumentException("source address error", e);
    }
  }

}
