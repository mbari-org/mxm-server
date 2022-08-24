package org.mbari.mxm.provider_client.rest;

import lombok.AllArgsConstructor;

import java.util.HashMap;

public class MissionPayload {

  public String missionTplId;
  public String missionId;
  public String assetId;
  public String description;
  public String schedType;
  public String schedDate;
  public HashMap<String, MissionArgValueAndUnits> arguments;

  // TODO remove
  //public String destinationAddress;


  @AllArgsConstructor
  public static class MissionArgValueAndUnits {
    public String value;
    public String units;
  }
}
