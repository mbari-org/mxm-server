package org.mbari.mxm.db.argument;

import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.db.DbUtl;
import org.mbari.mxm.db.mission.Mission;
import org.mbari.mxm.db.support.DbSupport;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class ArgumentService {

  @Inject
  DbSupport dbSupport;

  public List<Argument> getArguments(String providerId, String missionTplId, String missionId) {
    return dbSupport.getJdbi()
      .withExtension(ArgumentDao.class, dao -> dao.getArguments(providerId, missionTplId, missionId));
  }

  public List<List<Argument>> getArgumentsMultiple(List<Mission> missions) {

    final var tuples = missions.stream()
      .map(m -> String.format("('%s', '%s', '%s')", m.providerId, m.missionTplId, m.missionId))
      .collect(Collectors.toList());

    // TODO use group by in the query itself; for now, grouping in code below
    // TODO order by corresponding param_order
    var sql = """
      select * from arguments
      where (provider_id, mission_tpl_id, mission_id) in (<tuples>)
      """;

    var flatList = dbSupport.getJdbi()
      .withHandle(handle ->
        handle.createQuery(sql)
          .defineList("tuples", tuples)
          .mapToBean(Argument.class)
          .list()
      );

    // group by "tuple id":
    var byTupleId = flatList.stream().collect(
      Collectors.groupingBy(
        m -> String.format("('%s', '%s', '%s')", m.providerId, m.missionTplId, m.missionId),
        Collectors.toList())
    );

    // and map tuples to corresponding Arguments:
    return tuples.stream().map(byTupleId::get).collect(Collectors.toList());
  }

  public Argument getArgument(String providerId, String missionTplId, String missionId, String paramName) {
    return dbSupport.getJdbi()
      .withExtension(ArgumentDao.class, dao ->
        dao.getArgument(providerId, missionTplId, missionId, paramName));
  }

  public Argument createArgument(Argument pl) {
    return dbSupport.getJdbi()
      .withExtension(ArgumentDao.class, dao -> dao.insertArgument(pl));
  }

  private final ArgumentMapper argumentMapper = new ArgumentMapper();

  public Argument updateArgument(Argument pl) {
    log.debug("updateArgument: pl={}", pl);
    return doUpdate(pl);
  }

  private Argument doUpdate(Argument pl) {
    var upDef = DbUtl.updateDef("arguments", pl)
      .where("providerId")
      .where("missionTplId")
      .where("missionId")
      .where("paramName")
      .set(pl.paramValue, "paramValue")
      .set(pl.paramUnits, "paramUnits");

    return dbSupport.getJdbi()
      .withHandle(handle ->
        upDef.createQuery(handle)
          .execute((statementSupplier, ctx) -> {
            var rs = statementSupplier.get().executeQuery();
            return rs.next() ? ArgumentMapper.instance.map(rs, ctx) : null;
          })
      );
  }

  public Argument deleteArgument(Argument pl) {
    return dbSupport.getJdbi()
      .withExtension(ArgumentDao.class,
        dao -> dao.deleteArgument(pl.providerId, pl.missionTplId, pl.missionId, pl.paramName)
      );
  }
}

