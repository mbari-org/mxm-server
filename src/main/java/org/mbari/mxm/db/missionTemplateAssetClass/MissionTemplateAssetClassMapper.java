package org.mbari.mxm.db.missionTemplateAssetClass;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MissionTemplateAssetClassMapper implements RowMapper<MissionTemplateAssetClass> {

  @Override
  public MissionTemplateAssetClass map(ResultSet rs, StatementContext ctx) throws SQLException {
    return new MissionTemplateAssetClass(
      rs.getString("provider_id"),
      rs.getString("mission_tpl_id"),
      rs.getString("asset_class_name")
    );
  }
}
