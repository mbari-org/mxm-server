package org.mbari.mxm.db.missionStatusUpdate;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.db.mission.Mission;
import org.mbari.mxm.db.mission.MissionService;
import org.mbari.mxm.db.provider.ProviderService;
import org.mbari.mxm.db.support.DbSupport;
import org.mbari.mxm.rest.MissionStatus;

@ApplicationScoped
@Slf4j
public class MissionStatusUpdateService {

  @Inject DbSupport dbSupport;

  @Inject MissionService missionService;
  @Inject ProviderService providerService;

  public List<List<MissionStatusUpdate>> getMissionStatusUpdatesMultiple(List<Mission> missions) {
    // TODO make this more efficient

    ArrayList<List<MissionStatusUpdate>> res = new ArrayList<>();
    for (Mission mission : missions) {
      res.add(
          dbSupport
              .getJdbi()
              .withExtension(
                  MissionStatusUpdateDao.class,
                  dao ->
                      dao.getMissionStatusUpdates(
                          mission.getProviderId(),
                          mission.getMissionTplId(),
                          mission.getMissionId())));
    }
    return res;
  }

  public void missionStatusReported(
      Mission mission, List<MissionStatus.StatusUpdate> statusUpdates) {

    var del_res =
        deleteMissionStatusUpdates(mission.providerId, mission.missionTplId, mission.missionId);
    log.warn("del_res: {}", del_res);
    for (MissionStatus.StatusUpdate statusUpdate : statusUpdates) {
      var msu =
          new MissionStatusUpdate(
              mission.providerId,
              mission.missionTplId,
              mission.missionId,
              statusUpdate.date,
              statusUpdate.status);

      var msu_res = createMissionStatusUpdate(msu);
      log.warn("msu_res: {}", msu_res);
    }
    broadcastMissionUpdated(mission);
  }

  private MissionStatusUpdate createMissionStatusUpdate(MissionStatusUpdate pl) {
    log.debug("createMissionStatusUpdate: pl={}", pl);
    return dbSupport
        .getJdbi()
        .withExtension(MissionStatusUpdateDao.class, dao -> dao.insertMissionStatusUpdate(pl));
  }

  private Integer deleteMissionStatusUpdates(
      String providerId, String missionTplId, String missionId) {
    log.debug(
        "deleteMissionStatusUpdates: providerId={}, missionTplId={}, missionId={}",
        providerId,
        missionTplId,
        missionId);
    return dbSupport
        .getJdbi()
        .withExtension(
            MissionStatusUpdateDao.class,
            dao -> dao.deleteMissionStatusUpdates(providerId, missionTplId, missionId));
  }

  private void broadcastMissionUpdated(Mission mission) {
    log.debug("broadcastMissionUpdated: mission={}", mission);
    missionService.getBroadcaster().broadcastUpdated(mission);

    var p = providerService.getProvider(mission.providerId);
    if (p != null) {
      providerService.getBroadcaster().broadcastUpdated(p);
    }
  }
}
