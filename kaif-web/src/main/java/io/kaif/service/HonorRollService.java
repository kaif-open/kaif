package io.kaif.service;

import java.util.List;

import io.kaif.model.account.Authorization;
import io.kaif.model.vote.RotateVoteStats;
import io.kaif.model.zone.Zone;

public interface HonorRollService {

  List<RotateVoteStats> listRotateVoteStats(String username);

  List<RotateVoteStats> listHonorRoll(Zone zone);

}
