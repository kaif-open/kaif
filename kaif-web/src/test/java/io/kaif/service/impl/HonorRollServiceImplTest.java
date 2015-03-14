package io.kaif.service.impl;

import static java.util.stream.Collectors.*;
import static org.junit.Assert.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.vote.HonorRoll;
import io.kaif.model.vote.HonorRollDao;
import io.kaif.model.vote.HonorRollVoter;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.DbIntegrationTests;

public class HonorRollServiceImplTest extends DbIntegrationTests {

  public static final ZoneId BUCKET_ZONE = ZoneId.of("Asia/Taipei");

  @Autowired
  HonorRollServiceImpl service;

  @Autowired
  private HonorRollDao honorRollDao;

  private ZoneInfo zoneInfo;
  private Account citizen;

  @Before
  public void setUp() throws Exception {
    zoneInfo = savedZoneDefault("pic");
    citizen = savedAccountCitizen("citizen1");
    service.setClock(Clock.systemDefaultZone());
  }

  HonorRollVoter increaseArticleVoter(FlakeId flakeId, Zone zone) {
    return new HonorRollVoter.HonorRollVoterBuilder(citizen.getAccountId(),
        flakeId,
        zone,
        citizen.getUsername()).withDeltaArticleUpVoted(1).build();
  }

  @Test
  public void listRotateVoteStats() {
    Instant instant = LocalDate.of(2015, 3, 12)
        .atStartOfDay(BUCKET_ZONE)
        .toInstant();
    Clock clock = Clock.fixed(instant, BUCKET_ZONE);

    honorRollDao.updateRotateVoteStats(increaseArticleVoter(FlakeId.endOf(instant.toEpochMilli()),
        zoneInfo.getZone()));
    honorRollDao.updateRotateVoteStats(increaseArticleVoter(FlakeId.endOf(instant.toEpochMilli()
            + 1),
        zoneInfo.getZone()));
    honorRollDao.updateRotateVoteStats(increaseArticleVoter(FlakeId.endOf(instant.toEpochMilli()
            + 2),
        savedZoneDefault("pro").getZone()));
    honorRollDao.updateRotateVoteStats(increaseArticleVoter(FlakeId.endOf(instant.toEpochMilli()
            + 3),
        savedZoneDefault("abc").getZone()));

    service.setClock(clock);

    List<HonorRoll> honorRolls = service.listHonorRolls(citizen.getUsername());
    assertEquals(3, honorRolls.size());
    HonorRoll firstStats = honorRolls.get(0);
    assertEquals("abc", firstStats.getZone().value());
    assertEquals("2015-03-01", firstStats.getBucket());

    HonorRoll secondStats = honorRolls.get(1);
    assertEquals(2, secondStats.getArticleUpVoted());
    assertEquals("citizen1", secondStats.getUsername());
    assertEquals("pic", secondStats.getZone().value());

    HonorRoll thirdStats = honorRolls.get(2);
    assertEquals(1, thirdStats.getArticleUpVoted());
    assertEquals("pro", thirdStats.getZone().value());

    service.setClock(Clock.fixed(LocalDate.of(2015, 2, 12)
        .atStartOfDay(BUCKET_ZONE)
        .toInstant(), BUCKET_ZONE));
    assertEquals(0, service.listHonorRolls(citizen.getUsername()).size());
  }

  @Test
  public void listHonorRoll() {
    Instant instant = LocalDate.of(2015, 3, 12)
        .atStartOfDay(BUCKET_ZONE)
        .toInstant();
    Clock clock = Clock.fixed(instant, BUCKET_ZONE);

    honorRollDao.updateRotateVoteStats(new HonorRollVoter.HonorRollVoterBuilder(citizen.getAccountId(),
        FlakeId.endOf(instant.toEpochMilli()),
        zoneInfo.getZone(),
        citizen.getUsername()).withDeltaArticleUpVoted(10)
        .withDeltaDebateUpVoted(5)
        .withDeltaDebateDownVoted(3)
        .build());

    Account citizen2 = savedAccountCitizen("king");
    honorRollDao.updateRotateVoteStats(new HonorRollVoter.HonorRollVoterBuilder(citizen2.getAccountId(),
        FlakeId.endOf(instant.toEpochMilli()),
        zoneInfo.getZone(),
        citizen2.getUsername()).withDeltaArticleUpVoted(3)
        .withDeltaDebateUpVoted(2)
        .withDeltaDebateDownVoted(3)
        .build());

    service.setClock(clock);
    List<HonorRoll> honorRoll = service.listHonorRolls(zoneInfo.getZone());
    assertEquals(2, honorRoll.size());
    assertEquals("citizen1", honorRoll.get(0).getUsername());
    assertEquals(12, honorRoll.get(0).getScore());
    assertEquals("king", honorRoll.get(1).getUsername());
    assertEquals(2, honorRoll.get(1).getScore());

    service.setClock(Clock.fixed(LocalDate.of(2015, 4, 1)
        .atStartOfDay(BUCKET_ZONE)
        .toInstant(), BUCKET_ZONE));
    assertEquals(0, service.listHonorRolls(zoneInfo.getZone()).size());
  }

  @Test
  public void listHonorRoll_top_20() {
    Instant instant = LocalDate.of(2015, 3, 12)
        .atStartOfDay(BUCKET_ZONE)
        .toInstant();
    Clock clock = Clock.fixed(instant, BUCKET_ZONE);

    Random random = new Random();
    Comparator<HonorRollVoter> comparator = Comparator.comparing(voter -> voter.getDeltaArticleUpVoted()
        + voter.getDeltaDebateUpVoted()
        - voter.getDeltaDebateDownVoted());
    List<HonorRollVoter> allStats = IntStream.rangeClosed(1, 100)
        .mapToObj(index -> {
          Account account = savedAccountCitizen("user-" + index);
          HonorRollVoter voter = new HonorRollVoter.HonorRollVoterBuilder(account.getAccountId(),
              FlakeId.endOf(instant.toEpochMilli()),
              zoneInfo.getZone(),
              account.getUsername()).withDeltaArticleUpVoted(random.nextInt(100))
              .withDeltaDebateUpVoted(random.nextInt(100))
              .withDeltaDebateDownVoted(random.nextInt(100))
              .build();
          honorRollDao.updateRotateVoteStats(voter);
          return voter;
        })
        .sorted(comparator.reversed().thenComparing(HonorRollVoter::getUsername))
        .collect(toList());

    service.setClock(clock);
    List<HonorRoll> honorRoll = service.listHonorRolls(zoneInfo.getZone());
    assertEquals(allStats.stream().limit(20).map(HonorRollVoter::getUsername).collect(toList()),
        honorRoll.stream().map(HonorRoll::getUsername).collect(toList()));
  }

  @Test
  public void listHonorRoll_same_score_order_by_name() {
    Instant instant = LocalDate.of(2015, 3, 12)
        .atStartOfDay(BUCKET_ZONE)
        .toInstant();
    Clock clock = Clock.fixed(instant, BUCKET_ZONE);

    Comparator<HonorRollVoter> comparator = Comparator.comparing(voter -> voter.getDeltaArticleUpVoted()
        + voter.getDeltaDebateUpVoted()
        - voter.getDeltaDebateDownVoted());
    List<HonorRollVoter> allStats = IntStream.rangeClosed(1, 25)
        .mapToObj(index -> {
          Account account = savedAccountCitizen("user-" + index);
          HonorRollVoter voter = new HonorRollVoter.HonorRollVoterBuilder(account.getAccountId(),
              FlakeId.endOf(instant.toEpochMilli()),
              zoneInfo.getZone(),
              account.getUsername()).withDeltaArticleUpVoted(10)
              .build();
          honorRollDao.updateRotateVoteStats(voter);
          return voter;
        })
        .sorted(comparator.reversed().thenComparing(HonorRollVoter::getUsername))
        .collect(toList());

    service.setClock(clock);
    List<HonorRoll> honorRoll = service.listHonorRolls(zoneInfo.getZone());
    assertEquals(allStats.stream().limit(20).map(HonorRollVoter::getUsername).collect(toList()),
        honorRoll.stream().map(HonorRoll::getUsername).collect(toList()));
  }
}