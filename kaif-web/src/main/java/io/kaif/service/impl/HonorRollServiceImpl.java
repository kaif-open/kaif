package io.kaif.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;

import io.kaif.model.account.Authorization;
import io.kaif.model.vote.RotateVoteStats;
import io.kaif.model.vote.RotateVoteStatsDao;
import io.kaif.model.zone.Zone;
import io.kaif.service.HonorRollService;

@Service
@Transactional
public class HonorRollServiceImpl implements HonorRollService {

  private Clock clock = Clock.systemDefaultZone();

  private static final int PAGE_SIZE = 20;

  @Autowired
  RotateVoteStatsDao rotateVoteStatsDao;

  @VisibleForTesting
  void setClock(Clock clock) {
    this.clock = clock;
  }

  @Override
  public List<RotateVoteStats> listRotateVoteStats(Authorization authorization) {
    return rotateVoteStatsDao.listRotateVoteStatsByAccount(authorization.authenticatedId(),
        Instant.now(clock));
  }

  @Override
  public List<RotateVoteStats> listHonorRoll(Zone zone) {
    return rotateVoteStatsDao.listRotateVoteStatsByZone(zone, Instant.now(clock), PAGE_SIZE);
  }
}
