package io.kaif.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountDao;
import io.kaif.model.vote.HonorRoll;
import io.kaif.model.vote.HonorRollDao;
import io.kaif.model.zone.Zone;
import io.kaif.service.HonorRollService;

@Service
@Transactional
public class HonorRollServiceImpl implements HonorRollService {

  private Clock clock = Clock.systemDefaultZone();

  private static final int PAGE_SIZE = 20;

  @Autowired
  HonorRollDao honorRollDao;

  @Autowired
  AccountDao accountDao;

  @VisibleForTesting
  void setClock(Clock clock) {
    this.clock = clock;
  }

  @Override
  public List<HonorRoll> listHonorRollsByUsername(String username) {
    Account account = accountDao.loadByUsername(username);
    return honorRollDao.listHonorRollByAccount(account.getAccountId(),
        Instant.now(clock));
  }

  @Override
  public List<HonorRoll> listHonorRollsByZone(@Nullable Zone zone) {
    return honorRollDao.listHonorRollByZone(zone, Instant.now(clock), PAGE_SIZE);
  }
}
