package io.kaif.service.impl;

import static java.util.stream.Collectors.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.kaif.model.article.ArticleDao;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneDao;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.service.ZoneService;

@Service
@Transactional
public class ZoneServiceImpl implements ZoneService {

  @Autowired
  private ZoneDao zoneDao;

  @Autowired
  private ArticleDao articleDao;

  @Override
  public ZoneInfo loadZone(Zone zone) {
    return zoneDao.loadZoneWithCache(zone);
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
    Function<ZoneInfo, String> capitalizeFirstChar = zoneInfo -> zoneInfo.getZone()
        .value()
        .substring(0, 1)
        .toUpperCase();
    return zoneDao.listOrderByName()
        .stream()
        .collect(Collectors.groupingBy(capitalizeFirstChar, LinkedHashMap::new, toList()));
  }

  @Override
  public List<ZoneInfo> listRecommendZones() {
    int recommendSize = 10;
    List<ZoneInfo> results = articleDao.listHotZonesWithCache(recommendSize,
        Instant.now().minus(Duration.ofHours(24)));
    if (results.size() >= recommendSize) {
      return results;
    }
    // not enough hot zone within 24 hours, fall back to all zones...
    List<ZoneInfo> all = zoneDao.listOrderByName()
        .stream()
        .filter(zoneInfo -> !zoneInfo.isHideFromTop())
        .collect(toList());
    Collections.shuffle(all);
    return all.stream().limit(recommendSize).collect(toList());
  }
}
