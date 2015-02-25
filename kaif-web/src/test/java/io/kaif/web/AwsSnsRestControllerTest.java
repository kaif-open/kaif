package io.kaif.web;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import io.kaif.service.AccountService;

public class AwsSnsRestControllerTest {

  private static String q(String singleQuoted) {
    return singleQuoted.replaceAll("'", "\"");
  }

  private AwsSnsRestController controller;

  private MockHttpServletRequest request = new MockHttpServletRequest();

  @Mock
  private AccountService accountService;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    controller = new AwsSnsRestController(accountService);
  }

  @Test
  public void mailFeedback_confirmNotification() throws Exception {
    request.addHeader("x-amz-sns-message-type", "SubscriptionConfirmation");
    setJsonContent("{"
        + " 'Type':'SubscriptionConfirmation',"
        + " 'MessageId':'8b0f0abf-09f4-4208-bb88-a333a53615e0',"
        + " 'SubscribeURL':'http://httpbin.org/get'"
        + "}");
    controller.mailFeedback(request);
  }

  private void setJsonContent(String singleQuoted) {
    //    System.out.println(q(singleQuoted));
    request.setContent(q(singleQuoted).getBytes());
  }

  @Test
  public void mailFeedback_delivery() throws Exception {
    request.addHeader("x-amz-sns-message-type", "Notification");
    String message = q("{"
        + "  'notificationType':'Delivery',"
        + "  'mail':{"
        + "    'timestamp':'2014-10-26T12:20:16.071Z',"
        + "    'source':'noreply@kaif.io',"
        + "    'messageId':'000001494c665847-0a24c891-a43a-4b7a-99c0-60968e638bc4-000000',"
        + "    'destination':["
        + "      'success@simulator.amazonses.com'"
        + "    ]"
        + "  },"
        + "  'delivery':{"
        + "    'timestamp':'2014-10-26T12:20:16.689Z',"
        + "    'processingTimeMillis':618,"
        + "    'recipients':["
        + "      'success@simulator.amazonses.com'"
        + "    ],"
        + "    'smtpResponse':'250 2.6.0 Message received',"
        + "    'reportingMTA':'a8-52.smtp-out.amazonses.com'"
        + "  }"
        + "}");

    request.setContent(toJsonBytes(ImmutableMap.of("Type",
        "Notification",
        "MessageId",
        "21b2766f-a4c2-5e4f-8eaa-3aa9775ce46f'",
        "Message",
        message)));

    controller.mailFeedback(request);
    q("");
  }

  @Test
  public void mailFeedback_complaint() throws Exception {
    request.addHeader("x-amz-sns-message-type", "Notification");
    String message = q("{"
        + "  'notificationType':'Complaint',"
        + "  'complaint':{"
        + "    'complaintFeedbackType':'abuse',"
        + "    'complainedRecipients':["
        + "      {"
        + "        'emailAddress':'complaint@simulator.amazonses.com'"
        + "      }"
        + "    ],"
        + "    'userAgent':'Amazon SES Mailbox Simulator',"
        + "    'timestamp':'2014-10-26T14:24:39.000Z',"
        + "    'feedbackId':'000001494cd839bf-d1d08066-5d1b-11e4-8869-1119a4f60aa4-000000'"
        + "  },"
        + "  'mail':{"
        + "    'timestamp':'2014-10-26T14:24:38.000Z',"
        + "    'source':'noreply@kaif.io',"
        + "    'messageId':'000001494cd8372a-d52bda52-c14b-46fa-a624-b6a85d22d289-000000',"
        + "    'destination':["
        + "      'complaint@simulator.amazonses.com'"
        + "    ]"
        + "  }"
        + "}");

    request.setContent(toJsonBytes(ImmutableMap.of("Type",
        "Notification",
        "MessageId",
        "7eac2174-75c1-5c0a-9a5d-6db373a6be23'",
        "Message",
        message)));

    controller.mailFeedback(request);
    verify(accountService).complaintEmail(asList("complaint@simulator.amazonses.com"));
  }

  @Test
  public void mailFeedback_bounce() throws Exception {
    request.addHeader("x-amz-sns-message-type", "Notification");
    String message = q("{"
        + "  'notificationType':'Bounce',"
        + "  'bounce':{"
        + "    'bounceSubType':'General',"
        + "    'bounceType':'Permanent',"
        + "    'reportingMTA':'dsn; a8-26.smtp-out.amazonses.com',"
        + "    'bouncedRecipients':["
        + "      {"
        + "        'status':'5.1.1',"
        + "        'action':'failed',"
        + "        'diagnosticCode':'smtp; 550 5.1.1 user unknown',"
        + "        'emailAddress':'bounce@simulator.amazonses.com'"
        + "      }"
        + "    ],"
        + "    'timestamp':'2014-10-26T15:10:47.544Z',"
        + "    'feedbackId':'000001494d02704b-70c6bee9-bd31-4e2b-aeb3-2adcc5104d25-000000'"
        + "  },"
        + "  'mail':{"
        + "    'timestamp':'2014-10-26T15:10:45.000Z',"
        + "    'source':'noreply@kaif.io',"
        + "    'messageId':'000001494d026e8c-49338317-a0e9-4b69-b351-cb2e368778ee-000000',"
        + "    'destination':["
        + "      'bounce@simulator.amazonses.com'"
        + "    ]"
        + "  }"
        + "}");

    request.setContent(toJsonBytes(ImmutableMap.of("Type",
        "Notification",
        "MessageId",
        "cb0b3e1f-01f0-531c-90f5-72d810c7f6a7'",
        "Message",
        message)));

    controller.mailFeedback(request);
    verify(accountService).muteEmail(asList("bounce@simulator.amazonses.com"));
  }

  @Test
  public void mailFeedback_confirmNotification_subscribe_fail() throws Exception {
    request.addHeader("x-amz-sns-message-type", "SubscriptionConfirmation");
    request.setContent(toJsonBytes(ImmutableMap.of("SubscribeURL",
        "http://httpbin.org/status/500")));
    try {
      controller.mailFeedback(request);
      fail("IOException expected");
    } catch (IOException expected) {
    }
  }

  private byte[] toJsonBytes(ImmutableMap<String, Object> body) throws JsonProcessingException {
    return new ObjectMapper().writeValueAsBytes(body);
  }
}