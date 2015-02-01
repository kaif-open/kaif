package io.kaif.service;

import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneInfo;

public interface ZoneService {

  ZoneInfo getZone(Zone zone);

  ZoneInfo createDefault(String zone, String aliasName);

  ZoneInfo createKaif(String zone, String aliasName);

  void updateTheme(Zone zone, String theme);

}
