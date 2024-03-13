package org.mbari.mxm.db.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.db.DbUtl;
import org.mbari.mxm.db.support.DbSupport;

@ApplicationScoped
@Slf4j
public class UnitService {

  @Inject DbSupport dbSupport;

  public List<Unit> getAllUnits() {
    return dbSupport.getJdbi().withExtension(UnitDao.class, UnitDao::getAllUnits);
  }

  public List<List<Unit>> getDerivedUnitsMultiple(List<Unit> units) {
    var unitNames = units.stream().map(e -> String.format("'%s'", e.unitName)).toList();

    var sql =
        """
         select u2.*
         from units u, units u2
         where u.unit_name in (<unitNames>)
           and u.unit_name = u2.base_unit
         order by u2.unit_name;
      """;

    var flatList =
        dbSupport
            .getJdbi()
            .withHandle(
                handle ->
                    handle
                        .createQuery(sql)
                        .defineList("unitNames", unitNames)
                        .mapToBean(Unit.class)
                        .list());

    var byBaseUnit =
        flatList.stream()
            .collect(
                Collectors.groupingBy(e -> String.format("'%s'", e.baseUnit), Collectors.toList()));

    var res = new ArrayList<List<Unit>>();
    for (String tuple : unitNames) {
      res.add(byBaseUnit.get(tuple));
    }
    return res;
  }

  public Unit getUnit(String unitName) {
    return dbSupport.getJdbi().withExtension(UnitDao.class, dao -> dao.getUnit(unitName));
  }

  public void createUnits(List<Unit> units) {
    // first, create units without a base, then the others:

    var unitsMap = units.stream().collect(Collectors.partitioningBy(u -> u.baseUnit == null));

    unitsMap.get(true).forEach(this::createUnit);
    unitsMap.get(false).forEach(this::createUnit);
  }

  public Unit createUnit(Unit pl) {
    return dbSupport.getJdbi().withExtension(UnitDao.class, dao -> dao.insertUnit(pl));
  }

  public Unit updateUnit(Unit pl) {
    log.debug("updateUnit: pl={}", pl);
    return doUpdate(pl);
  }

  private Unit doUpdate(Unit pl) {
    var uDef =
        DbUtl.updateDef("units", pl)
            .where("unitName")
            .set(pl.abbreviation, "abbreviation")
            .set(pl.baseUnit, "baseUnit");

    if (uDef.noSets()) {
      return pl;
    }
    return dbSupport
        .getJdbi()
        .withHandle(
            handle ->
                uDef.createQuery(handle)
                    .execute(
                        (statementSupplier, ctx) -> {
                          var rs = statementSupplier.get().executeQuery();
                          return rs.next() ? UnitMapper.instance.map(rs, ctx) : null;
                        }));
  }

  public Unit deleteUnit(Unit pl) {
    return dbSupport.getJdbi().withExtension(UnitDao.class, dao -> dao.deleteUnit(pl.unitName));
  }
}
