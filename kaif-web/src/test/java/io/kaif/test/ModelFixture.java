package io.kaif.test;

import java.time.Instant;

import io.kaif.model.account.Account;
import io.kaif.model.zone.ZoneInfo;

/**
 * reusable test fixture for model, design for implements by test case
 */
public interface ModelFixture {

  default ZoneInfo zoneDefault(String zone) {
    return ZoneInfo.createDefault(zone, zone + "-alias", Instant.now());
  }

  default Account accountTourist(String username) {
    return Account.create(username, username + "@example.com", username + "pwd", Instant.now());
  }

}
