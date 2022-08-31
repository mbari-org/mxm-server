package org.mbari.mxm.db.assetClass;

import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(AssetClassMapper.class)
@RegisterBeanMapper(AssetClass.class)
public interface AssetClassDao {

  @SqlQuery("select * from asset_classes")
  List<AssetClass> getAllAssetClasses();

  @SqlQuery("""
      select * from asset_classes
      where provider_id = :providerId
      """)
  List<AssetClass> getAssetClasses(@Bind("providerId") String providerId);

  @SqlQuery(
      """
      select * from asset_classes
      where provider_id in (<providerIds>)
      order by class_name
      """)
  List<AssetClass> getAssetClassesMultiple(@BindList("providerIds") List<String> providerIds);

  @SqlQuery(
      """
      select * from asset_classes
      where provider_id = :providerId
        and class_name  = :className
      """)
  AssetClass getAssetClass(
      @Bind("providerId") String providerId, @Bind("className") String className);

  @SqlUpdate(
      """
      insert into asset_classes
      (provider_id, class_name, description)
      values (:providerId, :className, :description)
      returning *
      """)
  @GetGeneratedKeys
  AssetClass insertAssetClass(@BindBean AssetClass pl);

  @SqlUpdate(
      """
      delete from asset_classes where
      provider_id = :providerId and class_name = :className
      returning *
      """)
  @GetGeneratedKeys
  AssetClass deleteAssetClass(
      @Bind("providerId") String providerId, @Bind("className") String className);
}
