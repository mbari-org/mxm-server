package org.mbari.mxm.db.argument;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.db.DbUtl;
import org.mbari.mxm.db.mission.Mission;
import org.mbari.mxm.db.mission.MissionService;
import org.mbari.mxm.db.provider.ProviderService;
import org.mbari.mxm.db.support.DbSupport;

@ApplicationScoped
@Slf4j
public class ArgumentService {

  @Inject DbSupport dbSupport;

  @Inject MissionService missionService;
  @Inject ProviderService providerService;

  public List<Argument> getArguments(String providerId, String missionTplId, String missionId) {
    return dbSupport
        .getJdbi()
        .withExtension(
            ArgumentDao.class, dao -> dao.getArguments(providerId, missionTplId, missionId));
  }

  public List<List<Argument>> getArgumentsMultiple(List<Mission> missions) {

    final var tuples =
        missions.stream()
            .map(
                m -> String.format("('%s', '%s', '%s')", m.providerId, m.missionTplId, m.missionId))
            .collect(Collectors.toList());

    var sql =
        """
      select a.* from arguments a
      right join parameters using (provider_id, mission_tpl_id)
      where (a.provider_id, a.mission_tpl_id, a.mission_id) in (<tuples>)
        and a.param_name = parameters.param_name
      order by provider_id, mission_tpl_id, param_order
      """;
    /*
     select a.* from arguments a
     right join parameters using (provider_id, mission_tpl_id)
     where (a.provider_id, a.mission_tpl_id, a.mission_id) in (('tethystest_8080', '/Science/smear_sampling.xml', 'Q'))
       and a.param_name = parameters.param_name
     order by provider_id, mission_tpl_id, param_order
    */

    var flatList =
        dbSupport
            .getJdbi()
            .withHandle(
                handle ->
                    handle
                        .createQuery(sql)
                        .defineList("tuples", tuples)
                        .mapToBean(Argument.class)
                        .list());

    // group by "tuple id":
    var byTupleId =
        flatList.stream()
            .collect(
                Collectors.groupingBy(
                    m ->
                        String.format(
                            "('%s', '%s', '%s')", m.providerId, m.missionTplId, m.missionId),
                    Collectors.toList()));

    // and map tuples to corresponding Arguments:
    return tuples.stream().map(byTupleId::get).collect(Collectors.toList());
  }

  public Argument getArgument(
      String providerId, String missionTplId, String missionId, String paramName) {
    return dbSupport
        .getJdbi()
        .withExtension(
            ArgumentDao.class,
            dao -> dao.getArgument(providerId, missionTplId, missionId, paramName));
  }

  public List<Argument> getArgumentsWithParameterNames(
      String providerId, String missionTplId, List<String> paramNames) {
    if (paramNames.isEmpty()) {
      return List.of();
    }

    final var quotedParams =
        paramNames.stream().map(paramName -> String.format("'%s'", paramName)).toList();

    var sql =
        """
      select * from arguments
      where provider_id    = :providerId
        and mission_tpl_id = :missionTplId
        and param_name     in (<quotedParams>)
      """;

    return dbSupport
        .getJdbi()
        .withHandle(
            handle ->
                handle
                    .createQuery(sql)
                    .bind("providerId", providerId)
                    .bind("missionTplId", missionTplId)
                    .defineList("quotedParams", quotedParams)
                    .mapToBean(Argument.class)
                    .list());
  }

  public Argument createArgument(Argument pl) {
    log.debug("createArgument: pl={}", pl);
    var res = dbSupport.getJdbi().withExtension(ArgumentDao.class, dao -> dao.insertArgument(pl));
    broadcastMissionUpdated(res);
    return res;
  }

  public Argument updateArgument(Argument pl) {
    log.debug("updateArgument: pl={}", pl);
    return doUpdate(pl);
  }

  private Argument doUpdate(Argument pl) {
    var upDef =
        DbUtl.updateDef("arguments", pl)
            .where("providerId")
            .where("missionTplId")
            .where("missionId")
            .where("paramName")
            .setEvenIfNull(pl.paramValue, "paramValue")
            .setEvenIfNull(pl.paramUnits, "paramUnits");

    var res =
        dbSupport
            .getJdbi()
            .withHandle(
                handle ->
                    upDef
                        .createQuery(handle)
                        .execute(
                            (statementSupplier, ctx) -> {
                              var rs = statementSupplier.get().executeQuery();
                              return rs.next() ? ArgumentMapper.instance.map(rs, ctx) : null;
                            }));
    broadcastMissionUpdated(res);
    return res;
  }

  public Argument deleteArgument(Argument pl) {
    log.debug("deleteArgument: pl={}", pl);
    var res =
        dbSupport
            .getJdbi()
            .withExtension(
                ArgumentDao.class,
                dao ->
                    dao.deleteArgument(pl.providerId, pl.missionTplId, pl.missionId, pl.paramName));
    broadcastMissionUpdated(res);
    return res;
  }

  private void broadcastMissionUpdated(Argument arg) {
    log.debug("broadcastMissionUpdated: arg={}", arg);
    if (arg != null) {
      var m = missionService.getMission(arg.providerId, arg.missionTplId, arg.missionId);
      if (m != null) {
        m.setUpdatedDate(OffsetDateTime.now());
        missionService.updateMission(m);
      }
      missionService.getBroadcaster().broadcastUpdated(m);

      var p = providerService.getProvider(arg.providerId);
      if (p != null) {
        providerService.getBroadcaster().broadcastUpdated(p);
      }
    }
  }
}
