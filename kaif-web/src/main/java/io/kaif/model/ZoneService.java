package io.kaif.model;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.kaif.model.account.Authority;
import io.kaif.model.zone.ZoneDao;
import io.kaif.model.zone.ZoneInfo;

@Service
public class ZoneService {

  @Autowired
  private ZoneDao zoneDao;

  public ZoneInfo getZone(String zone) {
    return zoneDao.getZone(zone);
  }

  public ZoneInfo create(String zone,
      String aliasName,
      String theme,
      Authority read,
      Authority write) {
    return zoneDao.create(zone, aliasName, theme, read, write, Instant.now());
  }

  public void updateTheme(String zone, String theme) {
    zoneDao.updateTheme(zone, theme);
  }
}
