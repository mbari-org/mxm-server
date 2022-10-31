package org.mbari.mxm.db.missionStatusUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.mbari.mxm.db.mission.MissionStatusType;

public class MissionStatusUpdateMapper implements RowMapper<MissionStatusUpdate> {

  public static final MissionStatusUpdateMapper instance = new MissionStatusUpdateMapper();

  @Override
  public MissionStatusUpdate map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new MissionStatusUpdate(
        rs.getString("provider_id"),
        rs.getString("mission_tpl_id"),
        rs.getString("mission_id"),
        rs.getObject("update_date", OffsetDateTime.class),
        MissionStatusType.valueOf(rs.getString("status")));
  }
}
