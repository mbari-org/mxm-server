package org.mbari.mxm.db.asset;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

@RegisterRowMapper(AssetMapper.class)
@RegisterBeanMapper(Asset.class)
public interface AssetDao {

  @SqlQuery("select * from assets")
  List<Asset> getAllAssets();

  @SqlQuery(
    """
      select * from assets
      where provider_id = :providerId
      order by asset_id
      """
  )
  List<Asset> getAssets(@Bind("providerId") String providerId);

  @SqlQuery(
    """
      select * from assets
      where provider_id in (<providerIds>)
      order by asset_id
      """
  )
  List<Asset> getAssetsMultiple(@BindList("providerIds") List<String> providerIds);

  @SqlQuery(
    """
      select * from assets where
      provider_id = :providerId and asset_id = :assetId
      """
  )
  Asset getAsset(@Bind("providerId") String providerId,
                 @Bind("assetId") String assetId
  );

  @SqlUpdate(
    """
      insert into assets
      (provider_id, class_name, asset_id, description)
      values (:providerId, :className, :assetId, :description)
      returning *
      """
  )
  @GetGeneratedKeys
  Asset insertAsset(@BindBean Asset pl);

  @SqlUpdate(
    """
      delete from assets where
      provider_id = :providerId and asset_id = :assetId
      returning *
      """
  )
  @GetGeneratedKeys
  Asset deleteAsset(@Bind("providerId") String providerId,
                    @Bind("assetId") String assetId);

}
