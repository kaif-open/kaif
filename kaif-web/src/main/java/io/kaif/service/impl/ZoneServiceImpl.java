package io.kaif.service.impl;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.kaif.service.ZoneService;
import io.kaif.model.zone.ZoneDao;
import io.kaif.model.zone.ZoneInfo;

@Service
@Transactional
public class ZoneServiceImpl implements ZoneService {

  @Autowired
  private ZoneDao zoneDao;

  @Override
  public ZoneInfo getZone(String zone) {
    return zoneDao.getZone(zone);
  }

  @Override
  public ZoneInfo createDefault(String zone, String aliasName) {
    return zoneDao.create(ZoneInfo.createDefault(zone, aliasName, Instant.now()));
  }

  @Override
  public ZoneInfo createKaif(String zone, String aliasName) {
    return zoneDao.create(ZoneInfo.createKaif(zone, aliasName, Instant.now()));
  }

  @Override
  public void updateTheme(String zone, String theme) {
    zoneDao.updateTheme(zone, theme);
  }
}
