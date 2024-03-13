package org.mbari.mxm.db.missionStatusUpdate;

import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

  public List<MissionStatusUpdate> getMissionStatusUpdates(Mission mission) {
    return dbSupport
        .getJdbi()
        .withExtension(
            MissionStatusUpdateDao.class,
            dao ->
                dao.getMissionStatusUpdates(
                    mission.getProviderId(), mission.getMissionTplId(), mission.getMissionId()));
  }

  public List<List<MissionStatusUpdate>> getMissionStatusUpdatesMultiple(List<Mission> missions) {
    // TODO make this more efficient
    ArrayList<List<MissionStatusUpdate>> res = new ArrayList<>();
    for (Mission mission : missions) {
      res.add(getMissionStatusUpdates(mission));
    }
    return res;
  }

  public void missionStatusReported(
      Mission mission, List<MissionStatus.StatusUpdate> statusUpdates) {
    missionStatusReported(mission, statusUpdates, false);
  }

  public void missionStatusReportedAndBroadcast(
      Mission mission, List<MissionStatus.StatusUpdate> statusUpdates) {
    missionStatusReported(mission, statusUpdates, true);
  }

  private void missionStatusReported(
      Mission mission, List<MissionStatus.StatusUpdate> statusUpdates, boolean broadcast) {

    // delete all for the mission:
    deleteMissionStatusUpdates(mission.providerId, mission.missionTplId, mission.missionId);

    // now insert the updates:
    for (MissionStatus.StatusUpdate statusUpdate : statusUpdates) {
      var msu =
          new MissionStatusUpdate(
              mission.providerId,
              mission.missionTplId,
              mission.missionId,
              statusUpdate.date,
              statusUpdate.status);

      var msu_res = createMissionStatusUpdate(msu);
      log.trace("msu_res: {}", msu_res);
    }
    log.trace("inserted {}", statusUpdates.size());

    if (broadcast) {
      broadcastMissionUpdated(mission);
    }
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

    var res =
        dbSupport
            .getJdbi()
            .withExtension(
                MissionStatusUpdateDao.class,
                dao -> dao.deleteMissionStatusUpdates(providerId, missionTplId, missionId));
    log.trace("deleteMissionStatusUpdates: res={}", res);
    return res;
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
