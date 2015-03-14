package io.kaif.service.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.feed.FeedAsset;
import io.kaif.test.DbIntegrationTests;

public class FeedServiceImplTest extends DbIntegrationTests {
  @Autowired
  private FeedServiceImpl service;

  Account account;

  @Before
  public void setUp() throws Exception {
    account = savedAccountCitizen("userX");
  }

  @Test
  public void createReply() throws Exception {

    FlakeId debateId = nextFlakeId();
    FeedAsset asset = service.createReplyFeed(debateId, account.getAccountId());
    assertEquals(asList(asset), service.listFeeds(account, null));

    FeedAsset loaded = service.listFeeds(account, null).get(0);
    assertEquals(FeedAsset.AssetType.DEBATE_FROM_REPLY, loaded.getAssetType());
    assertEquals(debateId, loaded.getAssetId());
  }

  @Test
  public void acknowledge() throws Exception {
    FlakeId debateId = nextFlakeId();
    service.acknowledge(account, debateId);
    assertEquals("ack does nothing when no feed", 0, service.listFeeds(account, null).size());

    FeedAsset asset = service.createReplyFeed(debateId, account.getAccountId());
    assertFalse(asset.isAcked());
    service.acknowledge(account, asset.getAssetId());
    assertTrue(service.listFeeds(account, null).get(0).isAcked());
  }

  @Test
  public void countUnread() throws Exception {
    assertEquals(0, service.countUnread(account));
    FeedAsset a1 = service.createReplyFeed(nextFlakeId(), account.getAccountId());
    FeedAsset a2 = service.createReplyFeed(nextFlakeId(), account.getAccountId());
    assertEquals(2, service.countUnread(account));
    service.acknowledge(account, a1.getAssetId());
    assertEquals(1, service.countUnread(account));
    service.acknowledge(account, a2.getAssetId());
    assertEquals(0, service.countUnread(account));

    IntStream.range(0, 20)
        .forEach(i -> service.createReplyFeed(nextFlakeId(), account.getAccountId()));
    assertEquals(11, service.countUnread(account));
  }
}