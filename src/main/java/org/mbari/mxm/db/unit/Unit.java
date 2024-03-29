package org.mbari.mxm.db.unit;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;
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
public class Unit {

  @NotNull public String unitName;

  public Unit(String unitName) {
    this.unitName = unitName;
  }

  public String abbreviation;
  public String baseUnit;
}
