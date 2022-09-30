package org.mbari.mxm.db.missionTemplateAssetClass;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.mbari.mxm.db.assetClass.AssetClass;
import org.mbari.mxm.db.assetClass.AssetClassService;
import org.mbari.mxm.db.missionTemplate.MissionTemplate;
import org.mbari.mxm.db.support.DbSupport;

@ApplicationScoped
@Slf4j
public class MissionTemplateAssetClassService {

  @Inject DbSupport dbSupport;

  @Inject AssetClassService assetClassService;

  public List<MissionTemplateAssetClass> getAllMissionTemplateAssetClasses() {
    return dbSupport
        .getJdbi()
        .withExtension(
            MissionTemplateAssetClassDao.class,
            MissionTemplateAssetClassDao::getAllMissionTemplateAssetClasses);
  }

  public List<MissionTemplateAssetClass> getMissionTemplateAssetClasses(String providerId) {
    return dbSupport
        .getJdbi()
        .withExtension(
            MissionTemplateAssetClassDao.class,
            dao -> dao.getMissionTemplateAssetClasses(providerId));
  }

  public List<String> getAssetClassNames(String providerId, String missionTplId) {
    return dbSupport
        .getJdbi()
        .withExtension(
            MissionTemplateAssetClassDao.class,
            dao -> dao.getAssetClassNames(providerId, missionTplId));
  }

  // Support for getAssetClassesMultiple(List<MissionTemplate> missionTemplates):

  static @Data @EqualsAndHashCode(callSuper = true) @RegisterForReflection @ToString(
      callSuper = true) public class AssetClassWithMissionTplId extends AssetClass {
    public String missionTplId;
  }

  private static final RowMapper<AssetClassWithMissionTplId> assetClassWithMissionTplIdMapper =
      new RowMapper<AssetClassWithMissionTplId>() {
        @Override
        public AssetClassWithMissionTplId map(ResultSet rs, StatementContext ctx)
            throws SQLException {
          var e = new AssetClassWithMissionTplId();
          e.missionTplId = rs.getString("mission_tpl_id");
          e.providerId = rs.getString("provider_id");
          e.className = rs.getString("class_name");
          e.description = rs.getString("description");
          return e;
        }
      };

  public List<List<AssetClass>> getAssetClassesMultiple(List<MissionTemplate> missionTemplates) {
    final var tuples =
        missionTemplates.stream()
            .map(e -> String.format("('%s', '%s')", e.providerId, e.missionTplId))
            .collect(Collectors.toList());

    log.debug("tuples={}", tuples);

    // TODO(low prio) a more efficient query.

    var sql =
        """
      select *
      from mission_tpl_asset_class
      where (provider_id, mission_tpl_id) in (<tuples>);
      """;

    var missionTemplateAssetClasses =
        dbSupport
            .getJdbi()
            .withHandle(
                handle ->
                    handle
                        .createQuery(sql)
                        .defineList("tuples", tuples)
                        .mapToBean(MissionTemplateAssetClass.class)
                        .list());
    log.debug("missionTemplateAssetClasses={}", missionTemplateAssetClasses);

    var byProviderIdMissionTplId = new HashMap<String, ArrayList<AssetClass>>();

    missionTemplateAssetClasses.forEach(
        mac -> {
          var ac = assetClassService.getAssetClass(mac.providerId, mac.assetClassName);
          var tuple = String.format("('%s', '%s')", mac.providerId, mac.missionTplId);
          byProviderIdMissionTplId.computeIfAbsent(tuple, k -> new ArrayList<>()).add(ac);
        });

    return tuples.stream().map(byProviderIdMissionTplId::get).collect(Collectors.toList());
  }

  public MissionTemplateAssetClass createMissionTemplateAssetClass(MissionTemplateAssetClass pl) {
    return dbSupport
        .getJdbi()
        .withExtension(MissionTemplateAssetClassDao.class, dao -> dao.insert(pl));
  }

  public Integer deleteForMissionTemplate(String providerId, String missionTplId) {
    return dbSupport
        .getJdbi()
        .withExtension(
            MissionTemplateAssetClassDao.class,
            dao -> dao.deleteForMissionTemplate(providerId, missionTplId));
  }
}
