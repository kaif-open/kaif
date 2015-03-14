package io.kaif.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountDao;
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

  @Autowired
  AccountDao accountDao;

  @VisibleForTesting
  void setClock(Clock clock) {
    this.clock = clock;
  }

  @Override
  public List<RotateVoteStats> listRotateVoteStats(String username) {
    Account account = accountDao.loadByUsername(username);
    return rotateVoteStatsDao.listRotateVoteStatsByAccount(account.getAccountId(),
        Instant.now(clock));
  }

  @Override
  public List<RotateVoteStats> listHonorRoll(Zone zone) {
    return rotateVoteStatsDao.listRotateVoteStatsByZone(zone, Instant.now(clock), PAGE_SIZE);
  }
}
