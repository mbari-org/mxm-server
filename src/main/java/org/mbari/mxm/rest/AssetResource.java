package org.mbari.mxm.rest;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.db.asset.Asset;
import org.mbari.mxm.db.asset.AssetService;

@Path("/assets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class AssetResource extends BaseResource {

  @Inject AssetService assetService;

  @GET
  @Path("/")
  public Response getAssets() {
    var p = assetService.getAllAssets();
    if (p == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(p).build();
  }

  @GET
  @Path("/{assetId}")
  public Response getAsset(@PathParam("assetId") String assetId) {
    var p = assetService.getAsset(assetId);
    if (p == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(p).build();
  }

  @POST
  @Path("/")
  public Response createAsset(Asset pl) {
    var p = assetService.createAsset(pl);
    if (p == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(p).build();
  }

  @PUT
  @Path("/{assetId}")
  public Response updateAsset(@PathParam("assetId") String assetId, Asset pl) {
    if (pl == null || (pl.assetId != null && !pl.assetId.equals(assetId))) {
      return Response.status(Response.Status.BAD_REQUEST.getStatusCode())
          .entity("Invalid updateAsset payload")
          .build();
    }
    pl.assetId = assetId;
    var p = assetService.updateAsset(pl);
    if (p == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(p).build();
  }

  @DELETE
  @Path("/{assetId}")
  public Response deleteAssetClass(@PathParam("assetId") String assetId) {
    var p = assetService.deleteAsset(assetId);
    if (p == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    return Response.ok(p).build();
  }
}
