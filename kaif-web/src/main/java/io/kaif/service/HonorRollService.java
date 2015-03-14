package io.kaif.service;

import java.util.List;

import javax.annotation.Nullable;

import io.kaif.model.vote.HonorRoll;
import io.kaif.model.zone.Zone;

public interface HonorRollService {

  List<HonorRoll> listHonorRollsByUsername(String username);

  List<HonorRoll> listHonorRollsByZone(@Nullable Zone zone);

}
