package org.mbari.mxm.db.mission;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class MissionMapper implements RowMapper<Mission> {

  public static final MissionMapper instance = new MissionMapper();

  @Override
  public Mission map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new Mission(
      rs.getString("provider_id"),
      rs.getString("mission_tpl_id"),
      rs.getString("mission_id"),
      rs.getString("provider_mission_id"),
      MissionStatusType.valueOf(rs.getString("mission_status")),
      rs.getString("asset_id"),
      rs.getString("description"),
      MissionSchedType.valueOf(rs.getString("sched_type")),
      rs.getObject("sched_date", OffsetDateTime.class),
      rs.getObject("start_date", OffsetDateTime.class),
      rs.getObject("end_date", OffsetDateTime.class),
      rs.getObject("updated_date", OffsetDateTime.class)
    );
  }
}
