package org.mbari.mxm.provider_client.responses;

import lombok.Data;

// TODO this is all preliminary

@Data
public class MissionStatusResponse {

  public PMResponse result;

  @Data
  public static class PMResponse {
    public String missionId; // captured as `providerMissionId` in MXM mission model
    public String status;
  }
}
