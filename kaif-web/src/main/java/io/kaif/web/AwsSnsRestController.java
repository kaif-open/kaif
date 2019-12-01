package io.kaif.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;

import io.kaif.service.AccountService;

/**
 * see http://docs.aws.amazon.com/sns/latest/dg/SendMessageToHttp.html for how to handle aws sns
 * request
 * <p>
 * <p>
 * Created by ingram on 10/26/14.
 */
@RestController
@RequestMapping("/aws-sns")
public class AwsSnsRestController {

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class SnsBody {
    @JsonProperty("Type")
    public String type;

    @JsonProperty("MessageId")
    public String messageId;

    @JsonProperty("Message")
    public String message;

    @JsonProperty("SubscribeURL")
    public String subscribeUrl;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class Delivery {
    public List<String> recipients;

    @Override
    public String toString() {
      return "Delivery{" + "recipients=" + recipients + '}';
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class Complaint {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ComplainedRecipient {
      public String emailAddress;

      @Override
      public String toString() {
        return "emailAddress='" + emailAddress + '\'';
      }
    }

    public String complaintFeedbackType;
    public List<ComplainedRecipient> complainedRecipients;

    @Override
    public String toString() {
      return "Complaint{"
          + "complaintFeedbackType='"
          + complaintFeedbackType
          + '\''
          + ", complainedRecipients="
          + complainedRecipients
          + '}';
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class Bounce {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BouncedRecipient {
      public String emailAddress;
      public String status;
      public String action;
      public String diagnosticCode;

      @Override
      public String toString() {
        return "BouncedRecipient{"
            + "emailAddress='"
            + emailAddress
            + '\''
            + ", status='"
            + status
            + '\''
            + ", action='"
            + action
            + '\''
            + ", diagnosticCode='"
            + diagnosticCode
            + '\''
            + '}';
      }
    }

    public String bounceType;
    public String bounceSubType;
    public List<BouncedRecipient> bouncedRecipients;

    @Override
    public String toString() {
      return "Bounce{"
          + "bounceType='"
          + bounceType
          + '\''
          + ", bounceSubType='"
          + bounceSubType
          + '\''
          + ", bouncedRecipients="
          + bouncedRecipients
          + '}';
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class MailNotification {
    public String notificationType;
    public Delivery delivery;
    public Complaint complaint;
    public Bounce bounce;

    /**
     * feedback type has 5 types, only 'not-spam' is positive
     * <p>
     * http://www.iana.org/assignments/marf-parameters/marf-parameters.xml#marf-parameters-2
     */
    public List<String> badComplaintEmails() {
      return Optional.ofNullable(complaint)
          .filter(com -> !"not-spam".equalsIgnoreCase(com.complaintFeedbackType))
          .map(com -> com.complainedRecipients)
          .map(recs -> recs.stream()
              .map((Complaint.ComplainedRecipient rec) -> rec.emailAddress)
              .collect(Collectors.toList()))
          .orElse(Collections.emptyList());
    }

    /**
     * AWS document suggest remove bounceType=permanent
     */
    public List<String> permanentBouncedEmails() {
      return Optional.ofNullable(bounce)
          .filter(com -> "Permanent".equalsIgnoreCase(com.bounceType))
          .map(com -> com.bouncedRecipients)
          .map(recs -> recs.stream()
              .map((Bounce.BouncedRecipient rec) -> rec.emailAddress)
              .collect(Collectors.toList()))
          .orElse(Collections.emptyList());
    }
  }

  private static final Logger logger = LoggerFactory.getLogger(AwsSnsRestController.class);
  private static final String NO_SUCH_FIELD = "-no-such-field";
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private AccountService accountService;

  public AwsSnsRestController() {
    //spring
  }

  @VisibleForTesting
  AwsSnsRestController(AccountService accountService) {
    this.accountService = accountService;
  }

  /**
   * note that this url address <code>https://kaif.io/aws-sns/mail-feedback</code> is
   * used
   * as subscribe endpoint of topic 'kaif-mail-feedback' in
   * AWS SNS
   */
  @RequestMapping(value = "/mail-feedback", method = RequestMethod.POST)
  public void mailFeedback(HttpServletRequest request) throws IOException {
    String messageType = Optional.ofNullable(request.getHeader("x-amz-sns-message-type"))
        .map(Strings::emptyToNull)
        .orElse(NO_SUCH_FIELD);

    switch (messageType) {
      case "SubscriptionConfirmation":
        handleSubscriptionConfirmation(request);
        break;
      case "Notification":
        handleNotification(request);
        break;
      default:
        logger.warn("receive unknown sns, headers:\n"
            + dumpHeaders(request)
            + "\nbody:\n"
            + inputToString(request.getInputStream()));
        break;
    }
  }

  private String inputToString(InputStream in) throws IOException {
    return new String(ByteStreams.toByteArray(in), "UTF-8");
  }

  private void handleNotification(HttpServletRequest request) throws IOException {
    String body = inputToString(request.getInputStream());

    logger.debug("processing Notification... \nheaders:\n"
        + dumpHeaders(request)
        + "\nbody:\n"
        + body);

    SnsBody snsBody = objectMapper.readValue(body, SnsBody.class);

    MailNotification mailNotification = objectMapper.readValue(snsBody.message,
        MailNotification.class);

    String notificationType = Optional.ofNullable(mailNotification.notificationType)
        .map(Strings::emptyToNull)
        .orElseThrow(() -> new IllegalArgumentException("missing notificationType field"));

    switch (notificationType) {
      case "Bounce":
        accountService.muteEmail(mailNotification.permanentBouncedEmails());
        logger.info("receive... " + mailNotification.bounce);
        break;
      case "Complaint":
        accountService.complaintEmail(mailNotification.badComplaintEmails());
        logger.info("receive... " + mailNotification.complaint);
        break;
      case "Delivery":
        logger.debug("receive... " + mailNotification.delivery);
        break;
      default:
        logger.warn("receive unknown notificationType, ignore");
        break;
    }
  }

  private String dumpHeaders(HttpServletRequest request) {
    return toList(request.getHeaderNames()).stream()
        .collect(Collectors.toMap(name -> name, name -> toList(request.getHeaders(name))))
        .toString();
  }

  private List<String> toList(Enumeration<String> headerNames) {
    List<String> names = new ArrayList<>();
    while (headerNames.hasMoreElements()) {
      names.add(headerNames.nextElement());
    }
    return names;
  }

  private void handleSubscriptionConfirmation(HttpServletRequest request) throws IOException {
    String body = inputToString(request.getInputStream());

    logger.info("processing SubscriptionConfirmation... \nheaders:\n"
        + dumpHeaders(request)
        + "\nbody:\n"
        + body);

    SnsBody snsBody = objectMapper.readValue(body, SnsBody.class);

    String url = Optional.ofNullable(snsBody.subscribeUrl)
        .map(Strings::emptyToNull)
        .orElseThrow(() -> new IllegalArgumentException("missing SubscribeURL field"));

    int statusCode = requestUrl(url);
    if (statusCode >= 400) {
      logger.error("confirm subscribe failed:" + url + ", statusCode:" + statusCode);
      throw new IOException("subscribe failed");
    } else {
      logger.info("SubscriptionConfirmation done: " + url);
    }
  }

  private int requestUrl(String url) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    return connection.getResponseCode();
  }
}
