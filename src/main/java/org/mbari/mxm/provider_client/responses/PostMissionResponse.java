package org.mbari.mxm.provider_client.responses;

import lombok.Data;
import org.mbari.mxm.db.mission.MissionStatusType;

// preliminary

public record PostMissionResponse(PostMissionResult result) {

  @Data
  public static class PostMissionResult {
    public String missionTplId;
    public String missionId;
    public String providerMissionId;
    public MissionStatusType status;

    // TODO commonality with mission validation result
  }
}
