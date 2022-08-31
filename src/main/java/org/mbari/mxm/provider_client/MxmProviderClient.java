package org.mbari.mxm.provider_client;

import org.mbari.mxm.db.provider.ProviderApiType;
import org.mbari.mxm.graphql.ProviderPingException;
import org.mbari.mxm.provider_client.responses.*;
import org.mbari.mxm.provider_client.rest.PostMissionPayload;

public interface MxmProviderClient {

  String providerId();

  String httpEndpoint();

  ProviderApiType apiType();

  PingResponse ping() throws ProviderPingException;

  GeneralInfoResponse getGeneralInfo();

  AssetClassesResponse getAssetClasses();

  MissionTemplatesResponse getMissionTemplates(String subDir);

  MissionTemplateResponse getMissionTemplate(String filePath);

  UnitsResponse getUnits();

  MissionStatusResponse postMission(PostMissionPayload pl);

  MissionStatusResponse getMissionStatus(String missionId);

  void done();
}
