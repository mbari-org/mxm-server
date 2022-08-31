package org.mbari.mxm.db.assetClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetClassCreatePayload {

  public String className;
  public String description;

  public AssetClass toAssetClass(String providerId) {
    return AssetClass.builder()
        .providerId(providerId)
        .className(className)
        .description(description)
        .build();
  }
}
