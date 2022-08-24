package org.mbari.mxm.db.assetClass;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetClass {

  @NotNull
  public String providerId;
  @NotNull
  public String className;

  public AssetClass(String providerId, String className) {
    this.providerId = providerId;
    this.className = className;
  }

  public String description;
}
