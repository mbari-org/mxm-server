package org.mbari.mxm.db.parameter;

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
public class Parameter {

  @NotNull public String providerId;
  @NotNull public String missionTplId;
  @NotNull public String paramName;

  public Parameter(String providerId, String missionTplId, String paramName) {
    this.providerId = providerId;
    this.missionTplId = missionTplId;
    this.paramName = paramName;
  }

  public String type;
  public Boolean required;
  public String defaultValue;
  public String defaultUnits;
  public String valueCanReference;
  public String description;
  public Integer paramOrder;
}
