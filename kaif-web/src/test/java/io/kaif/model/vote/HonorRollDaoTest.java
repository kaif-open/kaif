package io.kaif.model.vote;

import static org.junit.Assert.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.model.zone.Zone;
import io.kaif.test.DbIntegrationTests;

public class HonorRollDaoTest extends DbIntegrationTests {

  @Autowired
  HonorRollDao dao;

  @Before
  public void setUp() {
    dao.evictAllCaches();
  }

  @Test
  public void listHonorRoll_cached() {
    LocalDate bucket = dao.monthlyBucket(Instant.now());
    List<HonorRoll> zoneHonorRollList = dao.listHonorRollByZoneWithCache(Zone.valueOf("qoo"),
        bucket,
        15);
    assertSame(zoneHonorRollList,
        dao.listHonorRollByZoneWithCache(Zone.valueOf("qoo"), bucket, 15));
    assertNotSame(zoneHonorRollList,
        dao.listHonorRollByZoneWithCache(Zone.valueOf("other-z"), bucket, 15));

    List<HonorRoll> all = dao.listHonorRollWithCache(bucket, 15);
    assertNotSame(zoneHonorRollList, all);
    assertSame(all, dao.listHonorRollWithCache(bucket, 15));
  }

}