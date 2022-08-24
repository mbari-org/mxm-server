package org.mbari.mxm.provider_client.responses;

import lombok.Data;

@Data
public class PingResponse {

  public Pong result;

  @Data
  public static class Pong {
    public String datetime;
  }
}
