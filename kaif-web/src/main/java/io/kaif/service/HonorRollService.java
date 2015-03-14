package io.kaif.service;

import java.util.List;

import io.kaif.model.vote.HonorRoll;
import io.kaif.model.zone.Zone;

public interface HonorRollService {

  List<HonorRoll> listHonorRolls(String username);

  List<HonorRoll> listHonorRolls(Zone zone);

}
