package org.mbari.mxm.provider_client.responses;

import java.util.List;
import lombok.Data;

@Data
public class MissionTemplatesResponse {

  public MissionTemplateList result;

  @Data
  public static class MissionTemplateList {
    public String directory;
    public List<MissionTemplateResponse.MissionTemplate> entries;
  }
}
