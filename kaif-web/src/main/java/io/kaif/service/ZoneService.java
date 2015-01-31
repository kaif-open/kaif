package io.kaif.service;

import io.kaif.model.zone.ZoneInfo;

public interface ZoneService {

  ZoneInfo getZone(String zone);

  ZoneInfo createDefault(String zone, String aliasName);

  ZoneInfo createKaif(String zone, String aliasName);

  void updateTheme(String zone, String theme);

}
