package org.mbari.mxm.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.mbari.mxm.db.mission.MissionStatusType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MissionStatus {
  public String missionTplId;
  public String missionId;
  public String providerMissionId;
  public MissionStatusType status;

  public ArrayList<StatusUpdate> statusUpdates = new ArrayList<>();

  // TODO other elements

  @ToString
  public static class StatusUpdate {
    public OffsetDateTime date;
    public MissionStatusType status;
  }
}
