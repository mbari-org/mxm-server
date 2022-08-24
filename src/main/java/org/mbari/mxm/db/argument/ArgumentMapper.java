package org.mbari.mxm.db.argument;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ArgumentMapper implements RowMapper<Argument> {

  public static final ArgumentMapper instance = new ArgumentMapper();

  @Override
  public Argument map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new Argument(
      rs.getString("provider_id"),
      rs.getString("mission_tpl_id"),
      rs.getString("mission_id"),
      rs.getString("param_name"),
      rs.getString("param_value"),
      rs.getString("param_units")
    );
  }
}
