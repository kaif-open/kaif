package io.kaif.test;

import java.time.Instant;
import java.util.Random;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.article.Article;
import io.kaif.model.zone.Zone;
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

  default Article article(Zone zone, String title) {
    Instant now = Instant.now();
    Account author = accountTourist("user" + new Random().nextInt(100));
    return Article.createExternalLink(zone,
        FlakeId.startOf(now.toEpochMilli()),
        author,
        title,
        "http://example.com/" + title,
        now);
  }

}
