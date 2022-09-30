package org.mbari.mxm.provider_client.rest;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.mbari.mxm.db.provider.ProviderApiType;
import org.mbari.mxm.graphql.ProviderPingException;
import org.mbari.mxm.provider_client.MxmProviderClient;
import org.mbari.mxm.provider_client.responses.*;

/** Client to interact with external provider using the REST MXM Provider API. */
@Slf4j
public class MxmProviderClientRest implements MxmProviderClient {

  private final String providerId;
  private final String httpEndpoint;

  private final ProviderClientRestService service;

  /**
   * Creates a new instance.
   *
   * @param providerId Provider ID.
   * @param httpEndpoint Base URL of the provider REST API.
   */
  public MxmProviderClientRest(String providerId, String httpEndpoint) {
    this.providerId = providerId;
    this.httpEndpoint = httpEndpoint;

    service =
        RestClientBuilder.newBuilder()
            .baseUri(URI.create(httpEndpoint))
            .build(ProviderClientRestService.class);
  }

  @Override
  public String providerId() {
    return providerId;
  }

  @Override
  public String httpEndpoint() {
    return httpEndpoint;
  }

  @Override
  public ProviderApiType apiType() {
    return ProviderApiType.REST;
  }

  @Override
  public PingResponse ping() throws ProviderPingException {
    try {
      return service.ping();
    } catch (Exception e) {
      throw new ProviderPingException(providerId, httpEndpoint, e);
    }
  }

  @Override
  public GeneralInfoResponse getGeneralInfo() {
    return service.getGeneralInfo();
  }

  @Override
  public AssetClassesResponse getAssetClasses() {
    return service.getAssetClasses();
  }

  @Override
  public MissionTemplatesResponse getMissionTemplates(String subDir) {
    if (subDir.equals("") || subDir.equals("/")) {
      return service.getMissionTemplatesRoot();
    } else {
      return service.getMissionTemplates(subDir);
    }
  }

  @Override
  public MissionTemplateResponse getMissionTemplate(String filePath) {
    return service.getMissionTemplate(filePath);
  }

  @Override
  public UnitsResponse getUnits() {
    return service.getUnits();
  }

  @Override
  public MissionValidationResponse validateMission(PostMissionPayload pl) {
    return service.validateMission(pl);
  }

  @Override
  public MissionStatusResponse postMission(PostMissionPayload pl) {
    return service.postMission(pl);
  }

  @Override
  public MissionStatusResponse getMissionStatus(String missionId) {
    return service.getMissionStatus(missionId);
  }

  @Override
  public void done() {
    try {
      ((Closeable) service).close();
    } catch (IOException e) {
      log.warn("Failed to close REST client", e);
    }
  }
}
