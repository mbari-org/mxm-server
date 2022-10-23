package org.mbari.mxm.rest;

import static javax.ws.rs.core.Response.Status.CREATED;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema;
import org.mbari.mxm.db.missionTemplate.MissionTemplate;
import org.mbari.mxm.db.missionTemplate.MissionTemplateCreatePayload;
import org.mbari.mxm.db.missionTemplate.MissionTemplateService;
import org.mbari.mxm.db.provider.Provider;
import org.mbari.mxm.db.provider.ProviderService;

@Path("/providers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class ProviderResource extends BaseResource {

  @Inject ProviderService service;

  @GET
  public Response getProviders() {
    try {
      List<Provider> providers = service.getProviders();
      return Response.ok().entity(providers).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GET
  @Path("/{providerId}")
  public Response getProviderById(@PathParam("providerId") String providerId) {
    var p = service.getProvider(providerId);
    if (p == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(p).build();
  }

  @POST
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
    // TODO propagate effect
    log.warn("POST missionStatus: providerId='{}' pl={}", providerId, pl);
    return Response.ok(pl).build();
  }

  @PUT
  @Path("/{providerId}")
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
  public Response deleteProvider(@PathParam("providerId") String providerId) {
    var pl = new Provider(providerId);
    var res = service.deleteProvider(pl);
    log.debug("deleteProvider: providerId={} =>{}", providerId, res);
    return Response.ok().entity(res).build();
  }

  ////////////////////////////////////////////////////////////////////////////////
  // MissionTemplates

  @Inject MissionTemplateService missionTemplateService;

  @GET
  @Path("/{providerId}/missionTemplates")
  public Response getMissionTemplates(@PathParam("providerId") String providerId) {
    log.debug("getMissionTemplates: providerId={}", providerId);
    try {
      var list = missionTemplateService.getMissionTemplates(providerId);
      return Response.ok().entity(list).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GET
  @Path("/{providerId}/missionTemplates/{missionTplId}")
  public Response getMissionTemplate(
      @PathParam("providerId") String providerId, @PathParam("missionTplId") String missionTplId) {
    log.debug("getMissionTemplate: providerId={}, missionTplId={}", providerId, missionTplId);
    var p = missionTemplateService.getMissionTemplate(providerId, missionTplId);
    if (p == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(p).build();
  }

  @POST
  @Path("/{providerId}/missionTemplates")
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
