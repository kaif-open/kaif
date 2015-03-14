package io.kaif.service.impl;

import static org.junit.Assert.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.vote.HonorRollVoter;
import io.kaif.model.vote.RotateVoteStats;
import io.kaif.model.vote.RotateVoteStatsDao;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.test.DbIntegrationTests;

public class HonorRollServiceImplTest extends DbIntegrationTests {

  public static final ZoneId BUCKET_ZONE = ZoneId.of("Asia/Taipei");

  @Autowired
  HonorRollServiceImpl service;

  @Autowired
  private RotateVoteStatsDao rotateVoteStatsDao;

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
        citizen.getUsername()).withDeltaArticleCount(1).build();
  }

  @Test
  public void listRotateVoteStats() {
    Instant instant = LocalDate.of(2015, 3, 12)
        .atStartOfDay(BUCKET_ZONE)
        .toInstant();
    Clock clock = Clock.fixed(instant, BUCKET_ZONE);

    rotateVoteStatsDao.updateRotateVoteStats(increaseArticleVoter(FlakeId.endOf(instant.toEpochMilli()),
        zoneInfo.getZone()));
    rotateVoteStatsDao.updateRotateVoteStats(increaseArticleVoter(FlakeId.endOf(instant.toEpochMilli()
            + 1),
        zoneInfo.getZone()));
    rotateVoteStatsDao.updateRotateVoteStats(increaseArticleVoter(FlakeId.endOf(instant.toEpochMilli()
            + 2),
        savedZoneDefault("pro").getZone()));
    rotateVoteStatsDao.updateRotateVoteStats(increaseArticleVoter(FlakeId.endOf(instant.toEpochMilli()
            + 3),
        savedZoneDefault("abc").getZone()));

    service.setClock(clock);

    List<RotateVoteStats> rotateVoteStatses = service.listRotateVoteStats(citizen);
    assertEquals(3, rotateVoteStatses.size());
    RotateVoteStats firstStats = rotateVoteStatses.get(0);
    assertEquals("abc", firstStats.getZone().value());
    assertEquals("2015-03-01", firstStats.getBucket());

    RotateVoteStats secondStats = rotateVoteStatses.get(1);
    assertEquals(2, secondStats.getArticleCount());
    assertEquals("citizen1", secondStats.getUsername());
    assertEquals("pic", secondStats.getZone().value());

    RotateVoteStats thirdStats = rotateVoteStatses.get(2);
    assertEquals(1, thirdStats.getArticleCount());
    assertEquals("pro", thirdStats.getZone().value());

    service.setClock(Clock.fixed(LocalDate.of(2015, 2, 12)
        .atStartOfDay(BUCKET_ZONE)
        .toInstant(), BUCKET_ZONE));
    assertEquals(0, service.listRotateVoteStats(citizen).size());
  }

  @Test
  public void listHonorRoll() {
    Instant instant = LocalDate.of(2015, 3, 12)
        .atStartOfDay(BUCKET_ZONE)
        .toInstant();
    Clock clock = Clock.fixed(instant, BUCKET_ZONE);

    rotateVoteStatsDao.updateRotateVoteStats(new HonorRollVoter.HonorRollVoterBuilder(citizen.getAccountId(),
        FlakeId.endOf(instant.toEpochMilli()),
        zoneInfo.getZone(),
        citizen.getUsername()).withDeltaArticleUpVoted(10)
        .withDeltaDebateUpVoted(5)
        .withDeltaDebateDownVoted(3)
        .build());

    Account citizen2 = savedAccountCitizen("king");
    rotateVoteStatsDao.updateRotateVoteStats(new HonorRollVoter.HonorRollVoterBuilder(citizen2.getAccountId(),
        FlakeId.endOf(instant.toEpochMilli()),
        zoneInfo.getZone(),
        citizen2.getUsername()).withDeltaArticleUpVoted(3)
        .withDeltaDebateUpVoted(2)
        .withDeltaDebateDownVoted(3)
        .build());

    service.setClock(clock);
    List<RotateVoteStats> honorRoll = service.listHonorRoll(zoneInfo.getZone());
    assertEquals(2, honorRoll.size());
    assertEquals("citizen1", honorRoll.get(0).getUsername());
    assertEquals(12, honorRoll.get(0).getScore());
    assertEquals("king", honorRoll.get(1).getUsername());
    assertEquals(2, honorRoll.get(1).getScore());

    service.setClock(Clock.fixed(LocalDate.of(2015, 4, 1)
        .atStartOfDay(BUCKET_ZONE)
        .toInstant(), BUCKET_ZONE));
    assertEquals(0, service.listHonorRoll(zoneInfo.getZone()).size());
  }

}