package org.mbari.mxm.db.argument;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;
import javax.validation.constraints.NotNull;
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
public class Argument {

  @NotNull public String providerId;
  @NotNull public String missionTplId;
  @NotNull public String missionId;
  @NotNull public String paramName;

  public Argument(String providerId, String missionTplId, String missionId, String paramName) {
    this.providerId = providerId;
    this.missionTplId = missionTplId;
    this.missionId = missionId;
    this.paramName = paramName;
  }

  public String paramValue;
  public String paramUnits;
}
