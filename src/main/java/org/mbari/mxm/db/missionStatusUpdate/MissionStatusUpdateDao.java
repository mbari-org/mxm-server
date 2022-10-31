package org.mbari.mxm.db.missionStatusUpdate;

import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(MissionStatusUpdateMapper.class)
@RegisterBeanMapper(MissionStatusUpdate.class)
public interface MissionStatusUpdateDao {
  @SqlQuery(
      """
    select * from mission_status_updates
    where provider_id    = :providerId
      and mission_tpl_id = :missionTplId
      and mission_id     = :missionId
    """)
  List<MissionStatusUpdate> getMissionStatusUpdates(
      @Bind("providerId") String providerId,
      @Bind("missionTplId") String missionTplId,
      @Bind("missionId") String missionId);

  @SqlUpdate(
      """
    insert into mission_status_updates
    (provider_id, mission_tpl_id, mission_id,
     update_date, status)
    values
    (:providerId, :missionTplId, :missionId,
     :updateDate, :status)
    returning *
    """)
  @GetGeneratedKeys
  MissionStatusUpdate insertMissionStatusUpdate(@BindBean MissionStatusUpdate pl);

  @SqlUpdate(
      """
    delete from mission_status_updates
    where provider_id    = :providerId
      and mission_tpl_id = :missionTplId
      and mission_id     = :missionId
    returning *
    """)
  Integer deleteMissionStatusUpdates(
      @Bind("providerId") String providerId,
      @Bind("missionTplId") String missionTplId,
      @Bind("missionId") String missionId);
}
