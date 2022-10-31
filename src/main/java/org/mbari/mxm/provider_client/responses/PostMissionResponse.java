package org.mbari.mxm.provider_client.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import lombok.Data;
import org.mbari.mxm.db.mission.MissionStatusType;
import org.mbari.mxm.rest.MissionStatus;

// preliminary

public record PostMissionResponse(PostMissionResult result) {

  @Data
  public static class PostMissionResult {
    public String missionTplId;
    public String missionId;
    public String providerMissionId;
    public MissionStatusType status;

    // TODO commonality with mission validation result

    @JsonIgnore
    public MissionStatusType getStatus() {
      return statusUpdates.get(statusUpdates.size() - 1).status;
    }

    public ArrayList<MissionStatus.StatusUpdate> statusUpdates = new ArrayList<>();
  }
}
