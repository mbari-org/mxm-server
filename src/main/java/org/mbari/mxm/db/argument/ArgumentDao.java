package org.mbari.mxm.db.argument;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

@RegisterRowMapper(ArgumentMapper.class)
@RegisterBeanMapper(Argument.class)
public interface ArgumentDao {

  @SqlQuery(
    """
      select * from arguments
      where provider_id    = :providerId
        and mission_tpl_id = :missionTplId
        and mission_id     = :missionId
      """
  )
  List<Argument> getArguments(@Bind("providerId") String providerId,
                              @Bind("missionTplId") String missionTplId,
                              @Bind("missionId") String missionId
  );

  @SqlQuery(
    """
      select * from arguments
      where provider_id    = :providerId
        and mission_tpl_id = :missionTplId
        and mission_id     = :missionId
        and param_name     = :paramName
      """
  )
  Argument getArgument(@Bind("providerId") String providerId,
                       @Bind("missionTplId") String missionTplId,
                       @Bind("missionId") String missionId,
                       @Bind("paramName") String paramName
  );

  @SqlUpdate(
    """
      insert into arguments
      (provider_id, mission_tpl_id, mission_id, param_name,
       param_value, param_units)
      values
      (:providerId, :missionTplId, :missionId, :paramName,
       :paramValue, :paramUnits)
      returning *
      """
  )
  @GetGeneratedKeys
  Argument insertArgument(@BindBean Argument pl);

  @SqlUpdate(
    """
      delete from arguments
      where provider_id    = :providerId
        and mission_tpl_id = :missionTplId
        and mission_id     = :missionId
        and param_name     = :paramName
      returning *
      """
  )
  @GetGeneratedKeys
  Argument deleteArgument(@Bind("providerId") String providerId,
                          @Bind("missionTplId") String missionTplId,
                          @Bind("missionId") String missionId,
                          @Bind("paramName") String paramName
  );

}
