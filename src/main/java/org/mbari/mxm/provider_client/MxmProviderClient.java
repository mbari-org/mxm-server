package org.mbari.mxm.provider_client;

import org.mbari.mxm.db.provider.ProviderApiType;
import org.mbari.mxm.graphql.ProviderPingException;
import org.mbari.mxm.provider_client.responses.*;
import org.mbari.mxm.provider_client.rest.MissionPayload;

public interface MxmProviderClient {

  String providerId();
  String httpEndpoint();
  ProviderApiType apiType();

  PingResponse ping() throws ProviderPingException;

  GeneralInfoResponse getGeneralInfo();

  AssetClassesResponse getAssetClasses();

  MissionTemplatesResponse getMissionTemplates();

  MissionTemplatesResponse getMissionTemplates(String subDir);

  MissionTemplateResponse getMissionTemplate(String filePath, boolean simple);

  UnitsResponse getUnits();

  PostMissionResponse postMission(MissionPayload pl);

  void done();
}
