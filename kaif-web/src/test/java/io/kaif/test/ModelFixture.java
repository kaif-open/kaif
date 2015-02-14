package io.kaif.test;

import java.time.Instant;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.account.Authority;
import io.kaif.model.article.Article;
import io.kaif.model.debate.Debate;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneInfo;

/**
 * reusable test fixture for model, design for implements by test case
 */
public interface ModelFixture extends TimeFixture {

  default ZoneInfo zoneDefault(String zone) {
    return ZoneInfo.createDefault(zone, zone + "-alias", Instant.now());
  }

  default Account accountTourist(String username) {
    return Account.create(username, username + "@example.com", username + "pwd", Instant.now());
  }

  default Account accountCitizen(String username) {
    Account account = Account.create(username,
        username + "@example.com",
        username + "pwd",
        Instant.now());
    return account.withAuthorities(Stream.of(Authority.CITIZEN, Authority.TOURIST)
        .collect(Collectors.toSet()));
  }

  default Account accountWithAuth(String passwordHash, Authority... authorities) {
    Account account = Account.create(passwordHash + "-user",
        passwordHash + "@example.com",
        passwordHash,
        Instant.now());
    return account.withAuthorities(Stream.of(authorities).collect(Collectors.toSet()));
  }

  default Debate debate(Article article, String content, Debate parent) {
    Instant now = Instant.now();
    Account debater = accountTourist("debater-" + new Random().nextInt(100));
    return Debate.create(article,
        FlakeId.startOf(now.toEpochMilli()),
        parent,
        content,
        debater,
        now);
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
