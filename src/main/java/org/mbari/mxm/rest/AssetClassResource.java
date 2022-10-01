package org.mbari.mxm.rest;

import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.db.assetClass.AssetClass;
import org.mbari.mxm.db.assetClass.AssetClassCreatePayload;
import org.mbari.mxm.db.assetClass.AssetClassService;

@Path("/assetClasses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class AssetClassResource extends BaseResource {

  @Inject AssetClassService assetClassService;

  @GET
  @Path("/")
  public Response getAssetClasses() {
    try {
      List<AssetClass> list = assetClassService.getAllAssetClasses();
      return Response.ok().entity(list).build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.serverError().status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GET
  @Path("/{className}")
  public Response getAssetClass(@PathParam("className") String className) {
    var p = assetClassService.getAssetClass(className);
    if (p == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(p).build();
  }

  @POST
  @Path("/")
  public Response createAssetClass(AssetClassCreatePayload pl) {
    var p = assetClassService.createAssetClass(pl.toAssetClass());
    if (p == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(p).build();
  }

  @PUT
  @Path("/{className}")
  public Response updateAssetClass(@PathParam("className") String className, AssetClass pl) {
    if (pl == null || (pl.className != null && !pl.className.equals(className))) {
      return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
          .entity("Invalid updateAssetClass payload")
          .build();
    }
    pl.className = className;
    var p = assetClassService.updateAssetClass(pl);
    if (p == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(p).build();
  }

  @DELETE
  @Path("/{className}")
  public Response deleteAssetClass(@PathParam("className") String className) {
    var p = assetClassService.deleteAssetClass(className);
    if (p == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(p).build();
  }
}
