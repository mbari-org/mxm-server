package org.mbari.mxm.db.asset;

import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(AssetMapper.class)
@RegisterBeanMapper(Asset.class)
public interface AssetDao {

  @SqlQuery("select * from assets")
  List<Asset> getAllAssets();

  @SqlQuery("""
      select * from assets
      where asset_id = :assetId
      """)
  Asset getAsset(@Bind("assetId") String assetId);

  @SqlUpdate(
      """
      insert into assets
      (class_name, asset_id, description)
      values (:className, :assetId, :description)
      returning *
      """)
  @GetGeneratedKeys
  Asset insertAsset(@BindBean Asset pl);

  @SqlUpdate(
      """
      delete from assets
      where asset_id = :assetId
      returning *
      """)
  @GetGeneratedKeys
  Asset deleteAsset(@Bind("assetId") String assetId);
}
