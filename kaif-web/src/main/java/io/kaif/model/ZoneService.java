package io.kaif.model;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.kaif.model.zone.ZoneDao;
import io.kaif.model.zone.ZoneInfo;

@Service
public class ZoneService {

  @Autowired
  private ZoneDao zoneDao;

  public ZoneInfo getZone(String zone) {
    return zoneDao.getZone(zone);
  }

  public ZoneInfo createDefault(String zone, String aliasName) {
    return zoneDao.create(ZoneInfo.createDefault(zone, aliasName, Instant.now()));
  }

  public ZoneInfo createKaif(String zone, String aliasName) {
    return zoneDao.create(ZoneInfo.createKaif(zone, aliasName, Instant.now()));
  }

  public void updateTheme(String zone, String theme) {
    zoneDao.updateTheme(zone, theme);
  }
}
