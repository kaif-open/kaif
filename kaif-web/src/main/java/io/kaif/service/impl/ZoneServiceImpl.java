package io.kaif.service.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneDao;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.service.ZoneService;

@Service
@Transactional
public class ZoneServiceImpl implements ZoneService {

  @Autowired
  private ZoneDao zoneDao;

  @Override
  public ZoneInfo loadZone(Zone zone) {
    return zoneDao.loadZone(zone);
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
  public void updateTheme(Zone zone, String theme) {
    zoneDao.updateTheme(zone, theme);
  }

  @Override
  public Map<String, List<ZoneInfo>> listZoneAtoZ() {
    return Collections.emptyMap();
  }
}
