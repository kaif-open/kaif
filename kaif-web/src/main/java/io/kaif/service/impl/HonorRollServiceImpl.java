package io.kaif.service.impl;

import static java.util.stream.Collectors.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
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

  private static final List<String> EXCLUDES_USER_NAME = Arrays.asList("koji", "IngramChen");
  private static final int PAGE_SIZE = 15;
  private Clock clock = Clock.systemDefaultZone();
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
    return honorRollDao.listHonorRollByAccount(account.getAccountId(), Instant.now(clock));
  }

  @Override
  public List<HonorRoll> listHonorRollsByZone(@Nullable Zone zone) {
    int pageSize = PAGE_SIZE + EXCLUDES_USER_NAME.size();
    LocalDate bucket = honorRollDao.monthlyBucket(Instant.now(clock));
    final List<HonorRoll> result;
    if (zone == null) {
      result = honorRollDao.listHonorRollWithCache(bucket, pageSize);
    } else {
      result = honorRollDao.listHonorRollByZoneWithCache(zone, bucket, pageSize);
    }
    return result.stream()
        .filter(honorRoll -> !EXCLUDES_USER_NAME.contains(honorRoll.getUsername()))
        .limit(PAGE_SIZE)
        .collect(toList());
  }
}
