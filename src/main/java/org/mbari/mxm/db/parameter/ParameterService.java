package org.mbari.mxm.db.parameter;

import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.db.DbUtl;
import org.mbari.mxm.db.missionTemplate.MissionTemplate;
import org.mbari.mxm.db.support.DbSupport;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class ParameterService {

  @Inject
  DbSupport dbSupport;

  public List<Parameter> getAllParameters() {
    return dbSupport.getJdbi()
      .withExtension(ParameterDao.class, ParameterDao::getAllParameters);
  }

  public List<Parameter> getParameters(String providerId, String missionTplId) {
    return dbSupport.getJdbi()
      .withExtension(ParameterDao.class, dao -> dao.getParameters(providerId, missionTplId));
  }

  public List<List<Parameter>> getParametersMultiple(List<MissionTemplate> missionTemplates) {

    final var tuples = missionTemplates.stream()
      .map(a -> String.format("('%s', '%s')", a.providerId, a.missionTplId))
      .collect(Collectors.toList());

    // TODO use group by in the query itself; for now, grouping in code below
    var sql = """
      select * from parameters
      where (provider_id, mission_tpl_id) in (<tuples>)
      order by param_order
      """;

    var flatList = dbSupport.getJdbi()
      .withHandle(handle ->
        handle.createQuery(sql)
          .defineList("tuples", tuples)
          .mapToBean(Parameter.class)
          .list()
      );

    // group by "tuple id":
    var byTupleId = flatList.stream().collect(
      Collectors.groupingBy(
        ac -> String.format("('%s', '%s')", ac.providerId, ac.missionTplId),
        Collectors.toList())
    );

    // and map tuples to corresponding Parameters:
    return tuples.stream().map(byTupleId::get).collect(Collectors.toList());
  }

  public Parameter getParameter(String providerId, String missionTplId, String paramName) {
    return dbSupport.getJdbi()
      .withExtension(ParameterDao.class, dao -> dao.getParameter(providerId, missionTplId, paramName));
  }

  public Parameter createParameter(Parameter pl) {
    return dbSupport.getJdbi()
      .withExtension(ParameterDao.class, dao -> dao.insertParameter(pl));
  }

  public Parameter updateParameter(Parameter pl) {
    log.debug("updateParameter: pl={}", pl);
    return doUpdate(pl);
  }

  private Parameter doUpdate(Parameter pl) {
    var uDef = DbUtl.updateDef("parameters", pl)
      .where("providerId")
      .where("missionTplId")
      .where("paramName")
      .set(pl.type, "type")
      .set(pl.required, "required")
      .set(pl.defaultValue, "defaultValue")
      .set(pl.defaultUnits, "defaultUnits")
      .set(pl.valueCanReference, "valueCanReference")
      .set(pl.description, "description");

    if (uDef.noSets()) {
      return pl;
    }
    return dbSupport.getJdbi()
      .withHandle(handle ->
        uDef.createQuery(handle)
          .execute((statementSupplier, ctx) -> {
            var rs = statementSupplier.get().executeQuery();
            return rs.next() ? ParameterMapper.instance.map(rs, ctx) : null;
          })
      );
  }

  public Parameter deleteParameter(Parameter pl) {
    return dbSupport.getJdbi()
      .withExtension(ParameterDao.class,
        dao -> dao.deleteParameter(pl.providerId, pl.missionTplId, pl.paramName)
      );
  }
}

