package org.mbari.mxm.provider_client.rest;

import com.fasterxml.jackson.annotation.JsonInclude;

@lombok.Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MxmInfo {

  // TODO or just a base endpoint on which to append particular routes depending on operation
  public String missionStatusEndpoint;
}
