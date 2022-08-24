package org.mbari.mxm.db.missionTemplateAssetClass;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

@RegisterRowMapper(MissionTemplateAssetClassMapper.class)
@RegisterBeanMapper(MissionTemplateAssetClass.class)
public interface MissionTemplateAssetClassDao {

  @SqlQuery("select * from mission_tpl_asset_class")
  List<MissionTemplateAssetClass> getAllMissionTemplateAssetClasses();

  @SqlQuery(
    """
      select * from mission_tpl_asset_class
      where provider_id = :providerId
      """
  )
  List<MissionTemplateAssetClass> getMissionTemplateAssetClasses(@Bind("providerId") String providerId);

  @SqlQuery(
    """
      select asset_class_name from mission_tpl_asset_class
      where provider_id    = :providerId
        and mission_tpl_id = :missionTplId
      """
  )
  List<String> getAssetClassNames(@Bind("providerId") String providerId,
                                  @Bind("missionTplId") String missionTplId);

  @SqlUpdate(
    """
      insert into mission_tpl_asset_class
      (provider_id, mission_tpl_id, asset_class_name)
      values (:providerId, :missionTplId, :assetClassName)
      returning *
      """
  )
  @GetGeneratedKeys
  MissionTemplateAssetClass insert(@BindBean MissionTemplateAssetClass pl);

  @SqlUpdate(
    """
      delete from mission_tpl_asset_class where
      provider_id = :providerId and mission_tpl_id = :missionTplId and asset_class_name = :assetClassName
      returning *
      """
  )
  @GetGeneratedKeys
  MissionTemplateAssetClass delete(@Bind("providerId") String providerId,
                                   @Bind("missionTplId") String missionTplId,
                                   @Bind("assetClassName") String assetClassName
  );

}
