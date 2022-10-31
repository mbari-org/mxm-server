package org.mbari.mxm.db.missionStatusUpdate;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.OffsetDateTime;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mbari.mxm.db.mission.MissionStatusType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RegisterForReflection
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MissionStatusUpdate {
  @NotNull public String providerId;
  @NotNull public String missionTplId;
  @NotNull public String missionId;

  public MissionStatusUpdate(String providerId, String missionTplId, String missionId) {
    this.providerId = providerId;
    this.missionTplId = missionTplId;
    this.missionId = missionId;
  }

  public OffsetDateTime updateDate;
  public MissionStatusType status;
}
