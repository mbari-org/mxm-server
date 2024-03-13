package org.mbari.mxm.db.missionTemplateAssetClass;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.graphql.api.Context;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.mbari.mxm.db.assetClass.AssetClass;
import org.mbari.mxm.db.assetClass.AssetClassService;
import org.mbari.mxm.db.missionTemplate.MissionTemplate;
import org.mbari.mxm.db.missionTemplate.MissionTemplateService;
import org.mbari.mxm.db.provider.Provider;
import org.mbari.mxm.db.provider.ProviderService;
import org.mbari.mxm.db.support.DbSupport;

@ApplicationScoped
@Slf4j
public class MissionTemplateAssetClassService {

  @Inject DbSupport dbSupport;

  @Inject AssetClassService assetClassService;

  @Inject MissionTemplateService missionTemplateService;

  @Inject ProviderService providerService;

  @Inject Context context;

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
          e.className = rs.getString("class_name");
          e.description = rs.getString("description");
          return e;
        }
      };

  public List<List<AssetClass>> getAssetClassesMultipleProviders(List<String> providerIds) {
    log.debug("providerIds={}", providerIds);

    // TODO(low prio) a more efficient query.

    var sql =
        """
    select *
    from mission_tpl_asset_class
    where provider_id in (<providerIds>);
    """;

    var missionTemplateAssetClasses =
        dbSupport
            .getJdbi()
            .withHandle(
                handle ->
                    handle
                        .createQuery(sql)
                        .defineList("providerIds", providerIds)
                        .mapToBean(MissionTemplateAssetClass.class)
                        .list());
    log.debug("missionTemplateAssetClasses={}", missionTemplateAssetClasses);

    var byProviderId = new HashMap<String, HashSet<AssetClass>>();

    missionTemplateAssetClasses.forEach(
        mac -> {
          var ac = assetClassService.getAssetClass(mac.assetClassName);
          var tuple = String.format("'%s'", mac.providerId);
          byProviderId.computeIfAbsent(tuple, k -> new HashSet<>()).add(ac);
        });

    return providerIds.stream()
        .map(providerId -> byProviderId.get(providerId).stream().toList())
        .collect(Collectors.toList());
  }

  @AllArgsConstructor
  @RegisterForReflection
  @ToString
  public static class NumAssetClassesByProviderId {
    public String providerId;
    public int count;
  }

  private static List<String> uniqueQuotedProviderIds(List<Provider> providers) {
    return providers.stream()
        .map(e -> String.format("'%s'", e.providerId))
        .collect(Collectors.toSet())
        .stream()
        .toList();
  }

  public List<Integer> getNumAssetClassesMultipleProviders(List<Provider> providers) {
    final var quotedProviderIds = uniqueQuotedProviderIds(providers);
    log.debug("getNumAssetClassesMultipleProviders quotedProviderIds={}", quotedProviderIds);

    var sql =
        """
        select provider_id, count(distinct(asset_class_name))
        from mission_tpl_asset_class
        where provider_id in (<quotedProviderIds>)
        group by provider_id;
        """;

    var counters =
        dbSupport
            .getJdbi()
            .withHandle(
                handle ->
                    handle
                        .createQuery(sql)
                        .defineList("quotedProviderIds", quotedProviderIds)
                        .map(
                            (rs, ctx) ->
                                new NumAssetClassesByProviderId(
                                    rs.getString("provider_id"), rs.getInt("count")))
                        .list());

    var byProviderId = counters.stream().collect(Collectors.toMap(c -> c.providerId, c -> c.count));

    return providers.stream().map(p -> byProviderId.get(p.providerId)).collect(Collectors.toList());
  }

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
          var ac = assetClassService.getAssetClass(mac.assetClassName);
          var tuple = String.format("('%s', '%s')", mac.providerId, mac.missionTplId);
          byProviderIdMissionTplId.computeIfAbsent(tuple, k -> new ArrayList<>()).add(ac);
        });

    return tuples.stream().map(byProviderIdMissionTplId::get).collect(Collectors.toList());
  }

  private static boolean selectedOnlyContainsSomeOf(Context context, String... names) {
    log.debug("context={}", context);
    final var jsonArray = context.getSelectedFields();
    if (jsonArray.size() > names.length) {
      return false;
    }
    // only pay attention to the fields that are strings:
    if (jsonArray.stream().anyMatch(e -> !(e instanceof JsonString))) {
      return false;
    }
    final var selectedFields = jsonArray.stream().map(e -> ((JsonString) e).getString()).toList();

    var nameList = new HashSet<>(Arrays.asList(names));
    var res = nameList.containsAll(selectedFields);
    log.debug("only contains selectedFields={} => {}", selectedFields, res);
    return res;
  }

  public List<List<MissionTemplate>> getMissionTemplatesMultiple(List<AssetClass> assetClasses) {
    var missionTemplateAssetClasses = getMissionTemplateAssetClasses(assetClasses);
    log.debug("missionTemplateAssetClasses={}", missionTemplateAssetClasses);

    var byClassName = new HashMap<String, HashSet<MissionTemplate>>();

    // just ad hoc way to lessen query inefficiency below
    var retrievedMissionTemplatesByPK = new HashMap<String, MissionTemplate>();

    // a direct response if only providerId and/or missionTplId are selected:
    final var simple = selectedOnlyContainsSomeOf(context, "providerId", "missionTplId");

    missionTemplateAssetClasses.forEach(
        mac -> {
          var providerIdMissionTplId =
              String.format("('%s', '%s')", mac.providerId, mac.missionTplId);

          var mt =
              retrievedMissionTemplatesByPK.computeIfAbsent(
                  providerIdMissionTplId,
                  k ->
                      simple
                          ? new MissionTemplate(mac.providerId, mac.missionTplId)
                          : missionTemplateService.getMissionTemplate(
                              mac.providerId, mac.missionTplId));

          if (mt != null) {
            byClassName.computeIfAbsent(mac.assetClassName, k -> new HashSet<>()).add(mt);
          }
        });

    return assetClasses.stream()
        .map(
            ac -> {
              var set = byClassName.get(ac.className);
              return set == null ? null : set.stream().toList();
            })
        .collect(Collectors.toList());
  }

  public List<List<Provider>> getProvidersMultiple(List<AssetClass> assetClasses) {
    var missionTemplateAssetClasses = getMissionTemplateAssetClasses(assetClasses);

    var byClassName = new HashMap<String, HashSet<Provider>>();

    final var simple = selectedOnlyContainsSomeOf(context, "providerId");

    missionTemplateAssetClasses.forEach(
        mac -> {
          var p =
              simple ? new Provider(mac.providerId) : providerService.getProvider(mac.providerId);
          if (p != null) {
            byClassName.computeIfAbsent(mac.assetClassName, k -> new HashSet<>()).add(p);
          }
        });

    return assetClasses.stream()
        .map(
            ac -> {
              var set = byClassName.get(ac.className);
              return set == null ? null : set.stream().toList();
            })
        .collect(Collectors.toList());
  }

  private List<MissionTemplateAssetClass> getMissionTemplateAssetClasses(
      List<AssetClass> assetClasses) {
    final var classNames =
        assetClasses.stream()
            .map(e -> String.format("'%s'", e.className))
            .collect(Collectors.toSet())
            .stream()
            .toList();

    log.debug("classNames={}", classNames);

    // TODO a more efficient query.

    var sql =
        """
      select *
      from mission_tpl_asset_class
      where asset_class_name in (<classNames>);
      """;

    return dbSupport
        .getJdbi()
        .withHandle(
            handle ->
                handle
                    .createQuery(sql)
                    .defineList("classNames", classNames)
                    .mapToBean(MissionTemplateAssetClass.class)
                    .list());
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
