package org.mbari.mxm.db.missionTemplate;

import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(MissionTemplateMapper.class)
@RegisterBeanMapper(MissionTemplate.class)
public interface MissionTemplateDao {

  @SqlQuery(
      """
      select * from mission_tpls
      order by provider_id, mission_tpl_id
      """)
  List<MissionTemplate> getAllMissionTemplates();

  @SqlQuery(
      """
      select * from mission_tpls
      where provider_id = :providerId
      order by mission_tpl_id
      """)
  List<MissionTemplate> getMissionTemplates(@Bind("providerId") String providerId);

  @SqlQuery(
      """
      select * from mission_tpls
      where provider_id     = :providerId
        and mission_tpl_id != :directory
        and mission_tpl_id ~ ('^' || :directory || '[^/]*/?$')
        -- the regex is to allow for direct subdirectories, but not further descendents.
        order by mission_tpl_id
        """)
  List<MissionTemplate> listMissionTplsDirectory(
      @Bind("providerId") String providerId, @Bind("directory") String directory);

  @SqlQuery(
      """
      select * from mission_tpls
      where provider_id in (<providerIds>)
      order by provider_id, mission_tpl_id
      """)
  List<MissionTemplate> getMissionTemplatesMultiple(
      @BindList("providerIds") List<String> providerIds);

  @SqlQuery(
      """
      select * from mission_tpls
      where provider_id    = :providerId
        and mission_tpl_id = :missionTplId
      """)
  MissionTemplate getMissionTemplate(
      @Bind("providerId") String providerId, @Bind("missionTplId") String missionTplId);

  @SqlUpdate(
      """
      insert into mission_tpls
      (provider_id, mission_tpl_id, description, retrieved_at)
      values (:providerId, :missionTplId, :description, :retrievedAt)
      returning *
      """)
  @GetGeneratedKeys
  MissionTemplate insertMissionTemplate(@BindBean MissionTemplate pl);

  @SqlUpdate(
      """
      delete from mission_tpls
      where provider_id    = :providerId
        and mission_tpl_id = :missionTplId
      returning *
      """)
  @GetGeneratedKeys
  MissionTemplate deleteMissionTemplate(
      @Bind("providerId") String providerId, @Bind("missionTplId") String missionTplId);

  @SqlUpdate(
      """
      delete from mission_tpls
      where provider_id = :providerId
        and mission_tpl_id like (:directory || '/%')
      """)
  Integer deleteDirectoryRecursive(
      @Bind("providerId") String providerId, @Bind("directory") String directory);
}
