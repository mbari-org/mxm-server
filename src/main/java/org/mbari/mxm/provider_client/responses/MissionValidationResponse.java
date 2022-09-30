package org.mbari.mxm.provider_client.responses;

import lombok.Data;

// preliminary

public record MissionValidationResponse(PMResponse result) {

  @Data
  public static class PMResponse {
    public String ok;
    public String error;
  }
}
