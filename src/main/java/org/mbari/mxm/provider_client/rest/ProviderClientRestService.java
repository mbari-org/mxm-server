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

  @POST
  @Path("mxmInfo")
  @Consumes(MediaType.APPLICATION_JSON)
  MxmInfo postMxmInfo(MxmInfo pl);

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
  @Path("missions/validate")
  @Consumes(MediaType.APPLICATION_JSON)
  MissionValidationResponse validateMission(PostMissionRequest pl);

  @POST
  @Path("missions")
  @Consumes(MediaType.APPLICATION_JSON)
  PostMissionResponse postMission(PostMissionRequest pl);

  @GET
  @Path("missions/{missionId: .*}")
  @Consumes(MediaType.APPLICATION_JSON)
  MissionStatusResponse getMissionStatus(@PathParam("missionId") String missionId);
}
