package org.mbari.mxm.provider_client.responses;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class MissionTemplateResponse {

  public MissionTemplate result;

  /**
   * Represents a proper mission template with relevant details,
   * or just a directory entry.
   */
  @Data
  @NoArgsConstructor
  public static class MissionTemplate {
    public String missionTplId;

    // possible fields when this a mission template per se:
    public String description;
    public List<String> assetClassNames;
    public List<Parameter> parameters;

    // possible field when this is a directory, not a mission template:
    @JsonDeserialize(contentAs = MissionTemplate.class)
    public List<MissionTemplate> entries;
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
