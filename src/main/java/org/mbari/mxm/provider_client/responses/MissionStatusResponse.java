package org.mbari.mxm.provider_client.responses;

import lombok.Data;

public record MissionStatusResponse(PMResponse result) {

  @Data
  public static class PMResponse {
    public String missionId;
    public String status;

    public String missionTplId;

    public String providerMissionId;
  }
}
