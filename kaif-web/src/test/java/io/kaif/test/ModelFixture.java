package io.kaif.test;

import java.time.Instant;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.account.Authority;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticleFlakeIdGenerator;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateFlakeIdGenerator;
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
    Account debater = accountCitizen("debater-" + new Random().nextInt(100));
    return Debate.create(article,
        new DebateFlakeIdGenerator(99).next(),
        parent,
        content,
        debater,
        now);
  }

  default Article article(Zone zone, String title) {
    Instant now = Instant.now();
    Account author = accountCitizen("user" + new Random().nextInt(100));
    return Article.createExternalLink(zone,
        zone.value() + "-alias",
        new ArticleFlakeIdGenerator(99).next(),
        author,
        title,
        "http://example.com/" + title,
        now);
  }

  default Article article(Zone zone, FlakeId articleId, String title) {
    Account author = accountCitizen("user" + new Random().nextInt(100));
    return Article.createExternalLink(zone,
        zone.value() + "-alias",
        articleId,
        author,
        title,
        "http://example.com/" + title,
        Instant.now());
  }

  default Article articleSpeak(Zone zone, FlakeId articleId, String title) {
    Account author = accountCitizen("user" + new Random().nextInt(100));
    return Article.createSpeak(zone,
        zone.value() + "-alias",
        articleId,
        author,
        title,
        title + "-content",
        Instant.now());
  }
}
