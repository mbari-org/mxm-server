package org.mbari.mxm.provider_client.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.mbari.mxm.provider_client.responses.*;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public interface ProviderClientRestService {

  @GET
  @Path("ping")
  PingResponse ping();

  @GET
  @Path("info")
  GeneralInfoResponse getGeneralInfo();

  @GET
  @Path("assetclasses")
  AssetClassesResponse getAssetClasses();

  @GET
  @Path("missiontemplates")
  MissionTemplatesResponse getMissionTemplatesRoot();

  @GET
  @Path("missiontemplates/{subDir: .*}")
  MissionTemplatesResponse getMissionTemplates(@PathParam("subDir") String subDir);

  @GET
  @Path("missiontemplate/{filePath: .*}")
  MissionTemplateResponse getMissionTemplate(@PathParam("filePath") String filePath);

  @GET
  @Path("units")
  UnitsResponse getUnits();

  @POST
  @Path("missions")
  @Consumes(MediaType.APPLICATION_JSON)
  MissionStatusResponse postMission(PostMissionPayload pl);

  @GET
  @Path("mission/{missionId: .*}")
  @Consumes(MediaType.APPLICATION_JSON)
  MissionStatusResponse getMissionStatus(@PathParam("missionId") String missionId);
}
