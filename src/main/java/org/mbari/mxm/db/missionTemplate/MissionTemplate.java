package org.mbari.mxm.db.missionTemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MissionTemplate {

  @NotNull
  public String providerId;
  @NotNull
  public String missionTplId;

  public MissionTemplate(String providerId, String missionTplId) {
    this.providerId = providerId;
    this.missionTplId = missionTplId;
  }

  public String description;
  public OffsetDateTime retrievedAt;
}
