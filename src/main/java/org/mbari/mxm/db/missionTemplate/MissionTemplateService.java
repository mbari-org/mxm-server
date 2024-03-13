package org.mbari.mxm.db.missionTemplate;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.Utl;
import org.mbari.mxm.db.DbUtl;
import org.mbari.mxm.db.mission.Mission;
import org.mbari.mxm.db.support.DbSupport;

@ApplicationScoped
@Slf4j
public class MissionTemplateService {

  @Inject DbSupport dbSupport;

  public List<MissionTemplate> getAllMissionTemplates() {
    return dbSupport
        .getJdbi()
        .withExtension(MissionTemplateDao.class, MissionTemplateDao::getAllMissionTemplates);
  }

  public List<MissionTemplate> getMissionTemplates(String providerId) {
    return dbSupport
        .getJdbi()
        .withExtension(MissionTemplateDao.class, dao -> dao.getMissionTemplates(providerId));
  }

  public List<MissionTemplate> listMissionTplsDirectory(String providerId, String directory) {
    final var dir = Utl.cleanPath(directory);
    return dbSupport
        .getJdbi()
        .withExtension(
            MissionTemplateDao.class, dao -> dao.listMissionTplsDirectory(providerId, dir));
  }

  public Map<String, List<MissionTemplate>> getMissionTemplatesMultiple(List<String> providerIds) {
    return dbSupport
        .getJdbi()
        .withExtension(
            MissionTemplateDao.class,
            dao -> {
              var missionTemplates = dao.getMissionTemplatesMultiple(providerIds);

              return missionTemplates.stream()
                  .collect(
                      Collectors.groupingBy(MissionTemplate::getProviderId, Collectors.toList()));
            });
  }

  public MissionTemplate getMissionTemplate(String providerId, String missionTplId) {
    return dbSupport
        .getJdbi()
        .withExtension(
            MissionTemplateDao.class, dao -> dao.getMissionTemplate(providerId, missionTplId));
  }

  public List<MissionTemplate> getMissionTemplates(List<Mission> missions) {
    final var tuples =
        missions.stream()
            .map(a -> String.format("('%s', '%s')", a.providerId, a.missionTplId))
            .collect(Collectors.toList());

    // we could probably simplify to use actual different keys for the query below,
    // but let's pass the whole list for now
    var sql =
        """
      select * from mission_tpls
      where (provider_id, mission_tpl_id) in (<tuples>)
      """;

    var res =
        dbSupport
            .getJdbi()
            .withHandle(
                handle ->
                    handle
                        .createQuery(sql)
                        .defineList("tuples", tuples)
                        .mapToBean(MissionTemplate.class)
                        .list());

    log.debug("tuples({}) = {}", tuples.size(), tuples);
    log.debug("res({}) = {}", res.size(), res);

    if (tuples.size() != res.size()) {
      // need to "align" for the result.
      // 1) get a map to resolve AssetClass by the "tuple id":
      var byTupleId =
          res.stream()
              .collect(
                  Collectors.groupingBy(
                      ac -> String.format("('%s', '%s')", ac.providerId, ac.missionTplId),
                      Collectors.toList()));
      // 2) then, map tuples to corresponding entity:
      // (note that each map value is necessarily non-empty per the above grouping)
      res = tuples.stream().map(p -> byTupleId.get(p).get(0)).collect(Collectors.toList());
    }
    return res;
  }

  public MissionTemplate createMissionTemplate(MissionTemplate pl) {
    return dbSupport
        .getJdbi()
        .withExtension(MissionTemplateDao.class, dao -> dao.insertMissionTemplate(pl));
  }

  public MissionTemplate updateMissionTemplate(MissionTemplate pl) {
    log.debug("updateMissionTemplate: pl={}", pl);
    var uDef =
        DbUtl.updateDef("mission_tpls", pl)
            .where("providerId")
            .where("missionTplId")
            .set(pl.description, "description")
            .set(pl.retrievedAt, "retrievedAt");

    if (uDef.noSets()) {
      log.warn(
          "updateMissionTemplate: no sets, providerId={}, missionTplId={}",
          pl.providerId,
          pl.missionTplId);
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
                          return rs.next() ? MissionTemplateMapper.instance.map(rs, ctx) : null;
                        }));
  }

  public MissionTemplate deleteMissionTemplate(MissionTemplate pl) {
    return dbSupport
        .getJdbi()
        .withExtension(
            MissionTemplateDao.class,
            dao -> dao.deleteMissionTemplate(pl.providerId, pl.missionTplId));
  }

  public Integer deleteDirectoryRecursive(String providerId, String directory) {
    var dirNoTrailingSlash = Utl.cleanPath(directory).replaceFirst("/+$", "");
    log.debug(
        "deleteDirectoryRecursive: providerId='{}', dirNoTrailingSlash='{}'",
        providerId,
        dirNoTrailingSlash);
    return dbSupport
        .getJdbi()
        .withExtension(
            MissionTemplateDao.class,
            dao -> dao.deleteDirectoryRecursive(providerId, dirNoTrailingSlash));
  }
}
