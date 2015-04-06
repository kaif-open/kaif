package io.kaif.service;

import java.util.List;
import java.util.Map;

import io.kaif.model.account.Authorization;
import io.kaif.model.exception.CreditNotEnoughException;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneInfo;

public interface ZoneService {

  ZoneInfo loadZone(Zone zone);

  ZoneInfo createDefault(String zone, String aliasName);

  ZoneInfo createKaif(String zone, String aliasName);

  void updateTheme(Zone zone, String theme);

  Map<String, List<ZoneInfo>> listZoneAtoZ();

  List<ZoneInfo> listRecommendZones();

  List<ZoneInfo> listCitizenZones();

  ZoneInfo createByUser(String zone, String aliasName, Authorization admin) throws
      CreditNotEnoughException;

  boolean isZoneAvailable(String zone);

  boolean canCreateZone(Authorization authorization);
}
