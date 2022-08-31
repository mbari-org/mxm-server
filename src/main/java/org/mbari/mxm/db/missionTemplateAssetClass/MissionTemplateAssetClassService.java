package org.mbari.mxm.db.missionTemplateAssetClass;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.mbari.mxm.db.missionTemplate.MissionTemplate;
import org.mbari.mxm.db.support.DbSupport;

@ApplicationScoped
@Slf4j
public class MissionTemplateAssetClassService {

  @Inject DbSupport dbSupport;

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

  // Support for getAssetNamesMultiple(List<MissionTemplate> missionTemplates):

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

  public List<List<AssetClass>> getAssetNamesMultiple(List<MissionTemplate> missionTemplates) {
    final var tuples =
        missionTemplates.stream()
            .map(e -> String.format("('%s', '%s')", e.providerId, e.missionTplId))
            .collect(Collectors.toList());

    log.debug("tuples={}", tuples);

    var sql =
        """
      select mac.mission_tpl_id as mission_tpl_id, ac.*
      from mission_tpl_asset_class mac, asset_classes ac
      where (mac.provider_id, mac.mission_tpl_id) in (<tuples>);
      """;

    var flatList =
        dbSupport
            .getJdbi()
            .withHandle(
                handle ->
                    handle
                        .createQuery(sql)
                        .defineList("tuples", tuples)
                        .registerRowMapper(assetClassWithMissionTplIdMapper)
                        .mapToBean(AssetClassWithMissionTplId.class)
                        .list());
    log.debug("flatList={}", flatList);

    var byTupleId =
        flatList.stream()
            .collect(
                Collectors.groupingBy(
                    e -> String.format("('%s', '%s')", e.providerId, e.missionTplId),
                    Collectors.toList()));

    return tuples.stream()
        .map(byTupleId::get)
        .map(
            list ->
                list == null
                    ? null
                    : list.stream().map(ac -> (AssetClass) ac).collect(Collectors.toList()))
        .collect(Collectors.toList());
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
