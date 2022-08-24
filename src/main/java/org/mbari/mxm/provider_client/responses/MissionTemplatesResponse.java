package org.mbari.mxm.provider_client.responses;

import lombok.Data;

import java.util.List;

@Data
public class MissionTemplatesResponse {

  public MissionTemplateList result;

  @Data
  public static class MissionTemplateList {
    public String subDir;
    public List<String> filenames;
  }
}
