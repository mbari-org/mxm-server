package org.mbari.mxm.db.parameter;

import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(ParameterMapper.class)
@RegisterBeanMapper(Parameter.class)
public interface ParameterDao {

  @SqlQuery("select * from parameters")
  List<Parameter> getAllParameters();

  @SqlQuery(
      """
      select * from parameters
      where provider_id    = :providerId
        and mission_tpl_id = :missionTplId
      order by param_order
      """)
  List<Parameter> getParameters(
      @Bind("providerId") String providerId, @Bind("missionTplId") String missionTplId);

  @SqlQuery(
      """
      select * from parameters
      where provider_id    = :providerId
        and mission_tpl_id = :missionTplId
        and param_name     = :paramName
      """)
  Parameter getParameter(
      @Bind("providerId") String providerId,
      @Bind("missionTplId") String missionTplId,
      @Bind("paramName") String paramName);

  @SqlUpdate(
      """
      insert into parameters
      (provider_id, mission_tpl_id, param_name,
       type, required, default_value, default_units,
        value_can_reference, description)
      values
      (:providerId, :missionTplId, :paramName,
       :type, :required, :defaultValue, :defaultUnits,
        :valueCanReference, :description)
      returning *
      """)
  @GetGeneratedKeys
  Parameter insertParameter(@BindBean Parameter pl);

  @SqlUpdate(
      """
      delete from parameters
      where provider_id    = :providerId
        and mission_tpl_id = :missionTplId
      """)
  Integer deleteForMissionTemplate(
      @Bind("providerId") String providerId, @Bind("missionTplId") String missionTplId);
}
