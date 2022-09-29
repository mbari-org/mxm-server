package org.mbari.mxm.provider_client.responses;

import lombok.Data;

public record MissionStatusResponse(PMResponse result) {

  @Data
  public static class PMResponse {
    // captured as `providerMissionId` in MXM mission model
    public String missionId;
    public String status;
  }
}
