package org.mbari.mxm.provider_client.rest;

import org.mbari.mxm.provider_client.responses.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

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
  MissionTemplatesResponse getMissionTemplates();

  @GET
  @Path("missiontemplates/{subDir: .*}")
  MissionTemplatesResponse getMissionTemplates(@PathParam("subDir") String subDir);

  @GET
  @Path("missiontemplate/{filePath: .*}")
  MissionTemplateResponse getMissionTemplate(@PathParam("filePath") String filePath,
                                             @QueryParam("simple") String simple
  );

  @GET
  @Path("units")
  UnitsResponse getUnits();

  @POST
  @Path("missions")
  @Consumes(MediaType.APPLICATION_JSON)
  PostMissionResponse postMission(MissionPayload pl);

}
