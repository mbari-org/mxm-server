package org.mbari.mxm.provider_client.rest;

import java.util.HashMap;
import lombok.AllArgsConstructor;

public class PostMissionPayload {

  public String missionTplId;
  public String assetId;
  public String description;
  public String schedType;
  public String schedDate;
  public HashMap<String, MissionArgValueAndUnits> arguments;

  @AllArgsConstructor
  public static class MissionArgValueAndUnits {
    public String value;
    public String units;
  }
}
