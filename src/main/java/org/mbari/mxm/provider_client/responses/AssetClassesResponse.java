package org.mbari.mxm.provider_client.responses;

import java.util.List;
import lombok.Data;

@Data
public class AssetClassesResponse {

  public List<AssetClass> result;

  @Data
  public static class AssetClass {
    public String assetClassName;
    public String description;
    public List<Asset> assets;
  }

  @Data
  public static class Asset {
    public String assetId;
  }
}
