package org.mbari.mxm.provider_client.responses;

import lombok.Data;

import java.util.List;

@Data
public class MissionTemplateResponse {

  public MissionTemplate result;

  @Data
  public static class MissionTemplate {
    public String missionTplId;
    public String description;
    public List<String> assetClassNames;
    public List<Parameter> parameters;
  }

  @Data
  public static class Parameter {
    public String paramName;
    public String type;
    public boolean required;
    public String defaultValue;
    public String defaultUnits;
    public String valueCanReference;
    public String description;
  }
}
