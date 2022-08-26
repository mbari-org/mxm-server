package org.mbari.mxm.db.mission;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Mission {

  @NotNull
  public String providerId;
  @NotNull
  public String missionTplId;
  @NotNull
  public String missionId;

  public Mission(String providerId, String missionTplId, String missionId) {
    this.providerId = providerId;
    this.missionTplId = missionTplId;
    this.missionId = missionId;
  }

  public MissionStatusType missionStatus;
  public String assetId;
  public String description;
  public MissionSchedType schedType;
  public OffsetDateTime schedDate;
  public OffsetDateTime startDate;
  public OffsetDateTime endDate;

  public OffsetDateTime updatedDate;

  @JsonIgnore
  public boolean noPatch() {
    return missionStatus == null
      && assetId == null
      && description == null
      && schedType == null
      && schedDate == null
      && startDate == null
      && endDate == null
      && updatedDate == null
      ;
  }
}
