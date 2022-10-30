package org.mbari.mxm.provider_client.rest;

import com.fasterxml.jackson.annotation.JsonInclude;

@lombok.Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MxmInfo {

  public String mxmRestEndpoint;
}
