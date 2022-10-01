package org.mbari.mxm.db.assetClass;

import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
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
      where class_name  = :className
      """)
  AssetClass getAssetClass(@Bind("className") String className);

  @SqlUpdate(
      """
      insert into asset_classes
      (class_name, description)
      values (:className, :description)
      returning *
      """)
  @GetGeneratedKeys
  AssetClass insertAssetClass(@BindBean AssetClass pl);

  @SqlUpdate(
      """
      delete from asset_classes
      where class_name = :className
      returning *
      """)
  @GetGeneratedKeys
  AssetClass deleteAssetClass(@Bind("className") String className);
}
