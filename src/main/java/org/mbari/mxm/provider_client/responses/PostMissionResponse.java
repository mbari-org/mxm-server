package org.mbari.mxm.provider_client.responses;

import lombok.Data;

// TODO this is all preliminary

@Data
public class PostMissionResponse {

  public PMResponse result;

  @Data
  public static class PMResponse {
    public String status;
  }
}
