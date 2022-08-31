package org.mbari.mxm.db.parameter;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class ParameterMapper implements RowMapper<Parameter> {

  public static final ParameterMapper instance = new ParameterMapper();

  @Override
  public Parameter map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new Parameter(
        rs.getString("provider_id"),
        rs.getString("mission_tpl_id"),
        rs.getString("param_name"),
        rs.getString("type"),
        rs.getObject("required", Boolean.class),
        rs.getString("default_value"),
        rs.getString("default_units"),
        rs.getString("value_can_reference"),
        rs.getString("description"),
        rs.getInt("param_order"));
  }
}
