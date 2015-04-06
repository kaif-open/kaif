package io.kaif.web.v1;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.Before;
import org.junit.Test;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.article.Article;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.model.debate.Debate;
import io.kaif.model.feed.FeedAsset;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.MvcIntegrationTests;

public class V1FeedResourceTest extends MvcIntegrationTests {
  private Account user;
  private ZoneInfo zone;
  private Article article;
  private Debate debate1;
  private Debate debate2;
  private FeedAsset asset1;
  private FeedAsset asset2;

  @Before
  public void setUp() throws Exception {
    user = accountCitizen("user1");
    zone = zoneDefault("fun");
    article = article(zone.getZone(), "art1");
    debate1 = debate(article, "deb1", null);
    debate2 = debate(article, "deb2", debate1);
    asset1 = FeedAsset.createReply(debate1.getDebateId(), user.getAccountId(), Instant.now());
    asset2 = FeedAsset.createReply(debate2.getDebateId(), user.getAccountId(), Instant.now());
  }

  @Test
  public void news() throws Exception {
    when(feedService.listFeeds(isA(ClientAppUserAccessToken.class),
        isNull(FlakeId.class))).thenReturn(asList(asset1, asset2));
    when(articleService.listDebatesByIdWithCache(asList(debate1.getDebateId(),
        debate2.getDebateId()))).thenReturn(asList(debate1, debate2));

    oauthPerform(user, get("/v1/feed/news")).andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].assetType", is("DEBATE_FROM_REPLY")))
        .andExpect(jsonPath("$.data[0].debate.content", is("deb1")));
  }

  @Test
  public void acknowledge() throws Exception {
    oauthPerform(user, post("/v1/feed/acknowledge").content(q("{'assetId':'foo123'}"))).andExpect(
        status().isOk());
    verify(feedService).acknowledge(isA(ClientAppUserAccessToken.class),
        eq(FlakeId.fromString("foo123")));
  }

  @Test
  public void newsUnreadCount() throws Exception {
    when(feedService.countUnread(isA(ClientAppUserAccessToken.class))).thenReturn(11);
    oauthPerform(user, get("/v1/feed/news-unread-count")).andExpect(status().isOk())
        .andExpect(jsonPath("$.data", is(11)));

  }
}