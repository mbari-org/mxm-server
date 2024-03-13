package org.mbari.mxm.db.provider;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import org.mbari.mxm.Broadcaster;
import org.mbari.mxm.ProgressBroadcaster;
import org.mbari.mxm.Utl;
import org.mbari.mxm.db.DbUtl;
import org.mbari.mxm.db.support.DbSupport;
import org.mbari.mxm.graphql.ProviderProgress;

@ApplicationScoped
@lombok.extern.slf4j.Slf4j
public class ProviderService {

  @Inject DbSupport dbSupport;

  @Getter
  private final Broadcaster<Provider> broadcaster =
      new Broadcaster<>() {
        @Override
        public String getEntityName() {
          return Provider.class.getSimpleName();
        }

        public String getPrimaryKey(Provider e) {
          return Utl.primaryKey(e.providerId);
        }
      };

  @Getter
  private final ProgressBroadcaster<ProviderProgress> progressBroadcaster =
      new ProgressBroadcaster<>() {
        @Override
        public String getEntityName() {
          return ProviderProgress.class.getSimpleName();
        }

        public String getPrimaryKey(ProviderProgress e) {
          return Utl.primaryKey(e.providerId);
        }
      };

  public List<Provider> getProviders() {
    return dbSupport.getJdbi().withExtension(ProviderDao.class, ProviderDao::list);
  }

  public Provider getProvider(String providerId) {
    log.debug("getProvider: providerId='{}'", providerId);
    return dbSupport.getJdbi().withExtension(ProviderDao.class, dao -> dao.get(providerId));
  }

  public List<Provider> getProviders(List<String> providerIds) {
    log.debug("getProviders: providerIds='{}'", providerIds);
    var res =
        dbSupport.getJdbi().withExtension(ProviderDao.class, dao -> dao.getProviders(providerIds));

    if (providerIds.size() != res.size()) {
      var byId =
          res.stream().collect(Collectors.groupingBy(Provider::getProviderId, Collectors.toList()));
      res = providerIds.stream().map(p -> byId.get(p).get(0)).collect(Collectors.toList());
    }
    return res;
  }

  public Provider createProvider(Provider pl) {
    log.debug("createProvider: pl={}", pl);
    assert pl.providerId != null;
    assert pl.apiType != null;
    var res = doCreate(pl);
    if (res != null) {
      broadcaster.broadcastCreated(res);
    }
    return res;
  }

  public Provider doCreate(Provider pl) {
    var cDef =
        DbUtl.createDef("providers")
            .set(pl.providerId, "providerId")
            .set(pl.apiType, "apiType")
            .set(pl.httpEndpoint, "httpEndpoint")
            .set(pl.description, "description")
            .set(pl.descriptionFormat, "descriptionFormat")
            .set(pl.usesSched, "usesSched")
            .set(pl.canValidate, "canValidate")
            .set(pl.usesUnits, "usesUnits")
            .set(pl.canReportMissionStatus, "canReportMissionStatus");

    return dbSupport
        .getJdbi()
        .withHandle(
            handle ->
                cDef.createUpdate(handle)
                    .bindBean(pl)
                    .execute(
                        (statementSupplier, ctx) -> {
                          var rs = statementSupplier.get().getResultSet();
                          return rs.next() ? ProviderMapper.instance.map(rs, ctx) : null;
                        }));
  }

  public Provider updateProvider(Provider pl) {
    log.debug("updateProvider: pl={}", pl);

    var res = doUpdate(pl);
    if (res != null) {
      broadcaster.broadcastUpdated(res);
    }
    return res;
  }

  private Provider doUpdate(Provider pl) {
    var uDef =
        DbUtl.updateDef("providers", pl)
            .where("providerId")
            .set(pl.httpEndpoint, "httpEndpoint")
            .set(pl.apiType, "apiType")
            .set(pl.description, "description")
            .set(pl.descriptionFormat, "descriptionFormat")
            .set(pl.usesSched, "usesSched")
            .set(pl.canValidate, "canValidate")
            .set(pl.usesUnits, "usesUnits")
            .set(pl.canReportMissionStatus, "canReportMissionStatus");

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
                          var next = rs.next();
                          if (!next) {
                            log.warn("doUpdate: no next result!");
                          }
                          return next ? ProviderMapper.instance.map(rs, ctx) : null;
                        }));
  }

  public Provider deleteProvider(Provider pl) {
    log.debug("deleteProvider: pl='{}'", pl);
    var res =
        dbSupport.getJdbi().withExtension(ProviderDao.class, dao -> dao.deleteById(pl.providerId));

    if (res != null) {
      broadcaster.broadcastDeleted(res);
    }
    return res;
  }
}
