package org.mbari.mxm.db.missionTemplate;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MissionTemplateCreatePayload {

  public String missionTplId;
  public String description;
  public OffsetDateTime retrievedAt;

  public MissionTemplate toMissionTemplate(String providerId) {
    return MissionTemplate.builder()
      .providerId(providerId)
      .missionTplId(missionTplId)
      .description(description)
      .retrievedAt(retrievedAt)
      .build();
  }
}
