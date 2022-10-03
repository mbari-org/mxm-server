package org.mbari.mxm.db.assetClass;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class AssetClassMapper implements RowMapper<AssetClass> {

  public static final AssetClassMapper instance = new AssetClassMapper();

  @Override
  public AssetClass map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new AssetClass(rs.getString("class_name"), rs.getString("description"));
  }
}
