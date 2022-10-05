package org.mbari.mxm.db.mission;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.Broadcaster;
import org.mbari.mxm.Utl;
import org.mbari.mxm.db.DbUtl;
import org.mbari.mxm.db.missionTemplate.MissionTemplate;
import org.mbari.mxm.db.support.DbSupport;

@ApplicationScoped
@Slf4j
public class MissionService {

  @Inject DbSupport dbSupport;

  @Getter
  private final Broadcaster<Mission> broadcaster =
      new Broadcaster<>() {
        @Override
        public String getEntityName() {
          return Mission.class.getSimpleName();
        }

        public String getPrimaryKey(Mission e) {
          return Utl.primaryKey(e.providerId, e.missionTplId, e.missionId);
        }
      };

  public List<Mission> getAllMissions() {
    return dbSupport.getJdbi().withExtension(MissionDao.class, MissionDao::getAllMissions);
  }

  public List<Mission> getMissionsForProvider(String providerId) {
    return dbSupport
        .getJdbi()
        .withExtension(MissionDao.class, dao -> dao.getMissionsForProvider(providerId));
  }

  public Map<String, List<Mission>> getMissionsForProviderMultiple(List<String> providerIds) {
    var sql =
        """
      select * from missions
      where provider_id in (<providerIds>)
      order by provider_id, mission_tpl_id, mission_id
      """;

    var flatList =
        dbSupport
            .getJdbi()
            .withHandle(
                handle ->
                    handle
                        .createQuery(sql)
                        .defineList(
                            "providerIds",
                            providerIds.stream()
                                .map(i -> "'" + i + "'")
                                .collect(Collectors.joining(", ")))
                        .mapToBean(Mission.class)
                        .list());

    return flatList.stream().collect(Collectors.groupingBy(m -> m.providerId, Collectors.toList()));
  }

  public List<Mission> getMissionsForTemplate(String providerId, String missionTplId) {
    return dbSupport
        .getJdbi()
        .withExtension(MissionDao.class, dao -> dao.getMissions(providerId, missionTplId));
  }

  public List<List<Mission>> getMissionsMultiple(List<MissionTemplate> missionTemplates) {

    final var tuples =
        missionTemplates.stream()
            .map(a -> String.format("('%s', '%s')", a.providerId, a.missionTplId))
            .collect(Collectors.toList());

    // TODO use group by in the query itself; for now, grouping in code below
    var sql =
        """
      select * from missions
      where (provider_id, mission_tpl_id) in (<tuples>)
      order by provider_id, mission_tpl_id, mission_id
      """;

    var flatList =
        dbSupport
            .getJdbi()
            .withHandle(
                handle ->
                    handle
                        .createQuery(sql)
                        .defineList("tuples", tuples)
                        .mapToBean(Mission.class)
                        .list());

    // group by "tuple id":
    var byTupleId =
        flatList.stream()
            .collect(
                Collectors.groupingBy(
                    m -> String.format("('%s', '%s')", m.providerId, m.missionTplId),
                    Collectors.toList()));

    // and map tuples to corresponding Missions:
    return tuples.stream().map(byTupleId::get).collect(Collectors.toList());
  }

  public Mission getMission(String providerId, String missionTplId, String missionId) {
    return dbSupport
        .getJdbi()
        .withExtension(
            MissionDao.class, dao -> dao.getMission(providerId, missionTplId, missionId));
  }

  public Mission createMission(Mission pl) {
    if (pl.updatedDate == null) {
      pl.updatedDate = OffsetDateTime.now();
    }
    var res = dbSupport.getJdbi().withExtension(MissionDao.class, dao -> dao.insertMission(pl));

    if (res != null) {
      broadcaster.broadcastCreated(res);
    }
    return res;
  }

  public Mission updateMission(Mission pl) {
    log.debug("updateMission: pl={}", pl);
    if (pl.updatedDate == null) {
      pl.updatedDate = OffsetDateTime.now();
    }
    var res = doUpdate(pl);

    if (res != null) {
      broadcaster.broadcastUpdated(res);
    }
    return res;
  }

  private Mission doUpdate(Mission pl) {
    var uDef =
        DbUtl.updateDef("missions", pl)
            .where("providerId")
            .where("missionTplId")
            .where("missionId")
            .set(pl.providerMissionId, "providerMissionId")
            .set(pl.missionStatus, "missionStatus")
            .set(pl.assetId, "assetId")
            .set(pl.description, "description")
            .set(pl.schedType, "schedType")
            .set(pl.schedDate, "schedDate")
            .set(pl.startDate, "startDate")
            .set(pl.endDate, "endDate")
            .set(pl.updatedDate, "updatedDate");

    if (pl.schedType != null && pl.schedType != MissionSchedType.DATE) {
      uDef.setNull("schedDate");
    }

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
                          return rs.next() ? MissionMapper.instance.map(rs, ctx) : null;
                        }));
  }

  public Mission deleteMission(Mission pl) {
    var res =
        dbSupport
            .getJdbi()
            .withExtension(
                MissionDao.class,
                dao -> dao.deleteMission(pl.providerId, pl.missionTplId, pl.missionId));
    if (res != null) {
      broadcaster.broadcastDeleted(res);
    }
    return res;
  }
}
