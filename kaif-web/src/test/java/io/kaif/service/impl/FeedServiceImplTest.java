package io.kaif.service.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.flake.FlakeId;
import io.kaif.model.KaifIdGenerator;
import io.kaif.model.account.Account;
import io.kaif.model.feed.FeedAsset;
import io.kaif.test.DbIntegrationTests;

public class FeedServiceImplTest extends DbIntegrationTests {
  @Autowired
  private FeedServiceImpl service;

  @Test
  public void createReply() throws Exception {
    Account account = savedAccountCitizen("userX");

    FlakeId debateId = new KaifIdGenerator(20).next();
    FeedAsset asset = service.createReplyFeed(debateId, account.getAccountId());
    assertEquals(asList(asset), service.listFeeds(account, null));

    FeedAsset loaded = service.listFeeds(account, null).get(0);
    assertEquals(FeedAsset.AssetType.DEBATE_FROM_REPLY, loaded.getAssetType());
    assertEquals(debateId, loaded.getAssetId());
  }
}