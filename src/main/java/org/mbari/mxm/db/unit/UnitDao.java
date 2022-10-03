package org.mbari.mxm.db.unit;

import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(UnitMapper.class)
@RegisterBeanMapper(Unit.class)
public interface UnitDao {

  @SqlQuery("select * from units")
  List<Unit> getAllUnits();

  @SqlQuery("""
      select * from units
      where unit_name = :unitName
      """)
  Unit getUnit(@Bind("unitName") String unitName);

  @SqlUpdate(
      """
      insert into units
      (unit_name, abbreviation, base_unit)
      values (:unitName, :abbreviation, :baseUnit)
      returning *
      """)
  @GetGeneratedKeys
  Unit insertUnit(@BindBean Unit pl);

  @SqlUpdate(
      """
      delete from units
      where unit_name = :unitName
      returning *
      """)
  @GetGeneratedKeys
  Unit deleteUnit(@Bind("unitName") String unitName);
}
