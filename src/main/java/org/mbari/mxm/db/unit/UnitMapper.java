package org.mbari.mxm.db.unit;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class UnitMapper implements RowMapper<Unit> {

  public static final UnitMapper instance = new UnitMapper();

  @Override
  public Unit map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new Unit(
        rs.getString("unit_name"), rs.getString("abbreviation"), rs.getString("base_unit"));
  }
}
