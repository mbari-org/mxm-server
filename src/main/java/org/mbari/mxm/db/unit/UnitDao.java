package org.mbari.mxm.db.unit;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

@RegisterRowMapper(UnitMapper.class)
@RegisterBeanMapper(Unit.class)
public interface UnitDao {

  @SqlQuery("select * from units")
  List<Unit> getAllUnits();

  @SqlQuery(
    """
      select * from units
      where provider_id = :providerId
      """
  )
  List<Unit> getUnits(@Bind("providerId") String providerId);

  @SqlQuery(
    """
      select * from units
      where provider_id in (<providerIds>)
      """
  )
  List<Unit> getUnitsMultiple(@BindList("providerIds") List<String> providerIds);

  @SqlQuery(
    """
      select * from units
      where provider_id = :providerId
        and unit_name   = :unitName
      """
  )
  Unit getUnit(@Bind("providerId") String providerId,
               @Bind("unitName") String unitName);

  @SqlUpdate(
    """
      insert into units
      (provider_id, unit_name, abbreviation, base_unit)
      values (:providerId, :unitName, :abbreviation, :baseUnit)
      returning *
      """
  )
  @GetGeneratedKeys
  Unit insertUnit(@BindBean Unit pl);

  @SqlUpdate(
    """
      delete from units where
      provider_id = :providerId and unit_name = :unitName
      returning *
      """
  )
  @GetGeneratedKeys
  Unit deleteUnit(@Bind("providerId") String providerId,
                  @Bind("unitName") String unitName);

}
