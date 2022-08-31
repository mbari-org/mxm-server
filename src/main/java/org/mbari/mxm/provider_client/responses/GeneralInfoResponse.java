package org.mbari.mxm.provider_client.responses;

import lombok.Data;

@Data
public class GeneralInfoResponse {

  public GeneralInfo result;

  @Data
  public static class GeneralInfo {
    public String providerName;
    public String providerDescription;
    public String descriptionFormat;

    public Capabilities capabilities;
  }

  @Data
  public static class Capabilities {
    public final boolean usesSched;
    public final boolean usesUnits;
    public final boolean canValidate;
    public final boolean canReportMissionStatus;
  }
}
