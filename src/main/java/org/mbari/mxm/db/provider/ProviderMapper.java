package org.mbari.mxm.db.provider;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProviderMapper implements RowMapper<Provider> {

  public static final ProviderMapper instance = new ProviderMapper();

  @Override
  public Provider map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new Provider(
      rs.getString("provider_id"),
      rs.getString("http_endpoint"),
      ProviderApiType.valueOf(rs.getString("api_type")),
      rs.getString("description"),
      rs.getString("description_format"),
      rs.getObject("uses_sched", Boolean.class),
      rs.getObject("can_validate", Boolean.class),
      rs.getObject("uses_units", Boolean.class),
      rs.getObject("can_report_mission_status", Boolean.class)
    );

  }
}
