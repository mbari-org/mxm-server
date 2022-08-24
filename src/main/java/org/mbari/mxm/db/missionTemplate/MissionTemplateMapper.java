package org.mbari.mxm.db.missionTemplate;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class MissionTemplateMapper implements RowMapper<MissionTemplate> {

  public static final MissionTemplateMapper instance = new MissionTemplateMapper();

  @Override
  public MissionTemplate map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new MissionTemplate(
      rs.getString("provider_id"),
      rs.getString("mission_tpl_id"),
      rs.getString("description"),
      rs.getObject("retrieved_at", OffsetDateTime.class)
    );
  }
}
