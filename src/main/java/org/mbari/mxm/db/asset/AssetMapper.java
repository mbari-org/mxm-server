package org.mbari.mxm.db.asset;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class AssetMapper implements RowMapper<Asset> {
  public static final AssetMapper instance = new AssetMapper();

  @Override
  public Asset map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new Asset(
        rs.getString("class_name"), rs.getString("asset_id"), rs.getString("description"));
  }
}
