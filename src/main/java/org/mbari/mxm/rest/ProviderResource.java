package org.mbari.mxm.rest;

import static javax.ws.rs.core.Response.Status.CREATED;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema;
import org.mbari.mxm.db.mission.Mission;
import org.mbari.mxm.db.mission.MissionService;
import org.mbari.mxm.db.mission.MissionStatusType;
import org.mbari.mxm.db.missionTemplate.MissionTemplate;
import org.mbari.mxm.db.missionTemplate.MissionTemplateCreatePayload;
import org.mbari.mxm.db.missionTemplate.MissionTemplateService;
import org.mbari.mxm.db.provider.Provider;
import org.mbari.mxm.db.provider.ProviderService;

/** REST API for use by external providers. */
@Path("/providers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class ProviderResource extends BaseResource {
  // special id only for internal testing purposes
  static final String TEST_PROVIDER_ID = "__test_prov__";

  @Inject ProviderService service;

  @Inject MissionService missionService;

  @POST
  @Operation(summary = "Register a provider")
  public Response createProvider(Provider pl) {
    if (pl == null || pl.providerId == null || pl.apiType == null) {
      return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
          .entity("Invalid createProvider payload")
          .build();
    }
    var res = service.createProvider(pl);
    log.debug("createProvider: pl={} =>{}", pl, res);
    return Response.ok(pl).status(CREATED).build();
  }

  @POST
  @Path("/{providerId}/missionStatus")
  @Operation(summary = "Used by the provider to inform MXM about a mission status update")
  @APIResponseSchema(MissionStatus.class)
  public Response missionStatus(@PathParam("providerId") String providerId, MissionStatus pl) {

    if (providerId.equals(TEST_PROVIDER_ID)) {
      var res =
          Mission.builder()
              .providerId(providerId)
              .missionTplId(pl.missionTplId)
              .missionId(pl.missionId)
              .missionStatus(MissionStatusType.RUNNING)
              .build();
      return Response.ok(res).build();
    }

    var res = missionService.missionStatusReported(providerId, pl);
    return Response.ok(res).build();
  }

  @PUT
  @Path("/{providerId}")
  @Operation(summary = "Update a provider")
  public Response updateProvider(@PathParam("providerId") String providerId, Provider pl) {
    if (pl == null || (pl.providerId != null && !pl.providerId.equals(providerId))) {
      return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
          .entity("Invalid updateProvider payload")
          .build();
    }
    pl.providerId = providerId;
    var res = service.updateProvider(pl);
    log.debug("createProvider: pl={} =>{}", writeValueAsString(pl), res);
    return Response.ok(pl).build();
  }

  @DELETE
  @Path("/{providerId}")
  @Operation(summary = "Delete a provider")
  public Response deleteProvider(@PathParam("providerId") String providerId) {
    var pl = new Provider(providerId);
    var res = service.deleteProvider(pl);
    log.debug("deleteProvider: providerId={} =>{}", providerId, res);
    return Response.ok().entity(res).build();
  }

  ////////////////////////////////////////////////////////////////////////////////
  // MissionTemplates

  @Inject MissionTemplateService missionTemplateService;

  @POST
  @Path("/{providerId}/missionTemplates")
  @Operation(summary = "Register a mission template")
  public Response createMissionTemplate(
      @PathParam("providerId") String providerId, MissionTemplateCreatePayload pl) {
    var p = missionTemplateService.createMissionTemplate(pl.toMissionTemplate(providerId));
    if (p == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(p).build();
  }

  @PUT
  @Path("/{providerId}/missionTemplates/{missionTplId}")
  @Operation(summary = "Update a mission template")
  public Response updateMissionTemplate(
      @PathParam("providerId") String providerId,
      @PathParam("missionTplId") String missionTplId,
      MissionTemplate pl) {
    if (pl == null
        || (pl.providerId != null && !pl.providerId.equals(providerId))
        || (pl.missionTplId != null && !pl.missionTplId.equals(missionTplId))) {
      return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
          .entity("Invalid updateMissionTemplate payload")
          .build();
    }
    pl.providerId = providerId;
    pl.missionTplId = missionTplId;
    var p = missionTemplateService.updateMissionTemplate(pl);
    if (p == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(p).build();
  }

  @DELETE
  @Path("/{providerId}/missionTemplates/{missionTplId}")
  @Operation(summary = "Delete a mission template")
  public Response deleteMissionTemplate(
      @PathParam("providerId") String providerId, @PathParam("missionTplId") String missionTplId) {
    var pl = new MissionTemplate(providerId, missionTplId);
    var p = missionTemplateService.deleteMissionTemplate(pl);
    if (p == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(p).build();
  }
}
