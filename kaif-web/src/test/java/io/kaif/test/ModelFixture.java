package io.kaif.test;

import java.time.Instant;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.kaif.flake.FlakeId;
import io.kaif.model.KaifIdGenerator;
import io.kaif.model.account.Account;
import io.kaif.model.account.Authority;
import io.kaif.model.article.Article;
import io.kaif.model.debate.Debate;
import io.kaif.model.feed.FeedAsset;
import io.kaif.model.vote.HonorRoll;
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
    return Debate.create(article, nextFlakeId(), parent, content, debater, now);
  }

  default FeedAsset assetReply(Debate debate) {
    return FeedAsset.createReply(debate.getDebateId(), debate.getReplyToAccountId(), Instant.now());
  }

  default HonorRoll honorRoll(Zone zone){
    Random random = new Random();
    Account account = accountCitizen("user" + random.nextInt(100));
    return new HonorRoll(account.getAccountId(), zone, "", account.getUsername(), random.nextInt(
        100), random
        .nextInt(100), random.nextInt(100));
  }

  default Article article(Zone zone, String title) {
    Instant now = Instant.now();
    Account author = accountCitizen("user" + new Random().nextInt(100));
    return Article.createExternalLink(zone,
        zone.value() + "-alias",
        nextFlakeId(),
        author,
        title,
        "http://example.com/" + title,
        now);
  }

  default FlakeId nextFlakeId() {
    return new KaifIdGenerator(99).next();
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
