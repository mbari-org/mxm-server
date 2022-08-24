package org.mbari.mxm.db.unit;


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
public class Unit {

  @NotNull
  public String providerId;
  @NotNull
  public String unitName;

  public Unit(String providerId, String unitName) {
    this.providerId = providerId;
    this.unitName = unitName;
  }

  public String abbreviation;
  public String baseUnit;
}
