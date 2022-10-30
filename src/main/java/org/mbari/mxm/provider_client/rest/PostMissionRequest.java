package org.mbari.mxm.provider_client.rest;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;

public class PostMissionRequest {

  public String providerId;
  public String missionTplId;
  public String missionId;
  public String assetId;
  public String description;
  public String schedType;
  public String schedDate;
  public List<MissionArgValueAndUnits> arguments = new ArrayList<>();

  @AllArgsConstructor
  public static class MissionArgValueAndUnits {
    public String paramName;
    public String value;
    public String units;
  }
}
