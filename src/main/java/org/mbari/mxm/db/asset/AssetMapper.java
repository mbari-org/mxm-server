package org.mbari.mxm.db.asset;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AssetMapper implements RowMapper<Asset> {
  public static final AssetMapper instance = new AssetMapper();

  @Override
  public Asset map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new Asset(
      rs.getString("provider_id"),
      rs.getString("class_name"),
      rs.getString("asset_id"),
      rs.getString("description")
    );
  }
}
