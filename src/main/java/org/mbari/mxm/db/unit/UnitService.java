package org.mbari.mxm.db.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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

  public List<Unit> getUnits(String providerId) {
    return dbSupport.getJdbi().withExtension(UnitDao.class, dao -> dao.getUnits(providerId));
  }

  public Map<String, List<Unit>> getUnitsMultiple(List<String> providerIds) {
    return dbSupport
        .getJdbi()
        .withExtension(
            UnitDao.class,
            dao -> {
              var list = dao.getUnitsMultiple(providerIds);

              return list.stream()
                  .collect(Collectors.groupingBy(Unit::getProviderId, Collectors.toList()));
            });
  }

  public List<List<Unit>> getDerivedUnitsMultiple(List<Unit> units) {
    var tuples =
        units.stream().map(e -> String.format("('%s', '%s')", e.providerId, e.unitName)).toList();

    var sql =
        """
         select u2.*
         from units u, units u2
         where (u.provider_id, u.unit_name) in (<tuples>)
           and u.provider_id = u2.provider_id
           and u.unit_name = u2.base_unit
         order by u2.provider_id, u2.unit_name;
      """;

    var flatList =
        dbSupport
            .getJdbi()
            .withHandle(
                handle ->
                    handle
                        .createQuery(sql)
                        .defineList("tuples", tuples)
                        .mapToBean(Unit.class)
                        .list());

    // here: by (providerId, baseUnit) tuples:
    var byTupleId =
        flatList.stream()
            .collect(
                Collectors.groupingBy(
                    e -> String.format("('%s', '%s')", e.providerId, e.baseUnit),
                    Collectors.toList()));

    var res = new ArrayList<List<Unit>>();
    for (String tuple : tuples) {
      res.add(byTupleId.get(tuple));
    }
    return res;
  }

  public Unit getUnit(String providerId, String unitName) {
    return dbSupport
        .getJdbi()
        .withExtension(UnitDao.class, dao -> dao.getUnit(providerId, unitName));
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
            .where("providerId")
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
    return dbSupport
        .getJdbi()
        .withExtension(UnitDao.class, dao -> dao.deleteUnit(pl.providerId, pl.unitName));
  }
}
