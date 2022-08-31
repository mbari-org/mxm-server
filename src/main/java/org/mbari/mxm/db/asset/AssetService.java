package org.mbari.mxm.db.asset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.db.DbUtl;
import org.mbari.mxm.db.mission.Mission;
import org.mbari.mxm.db.support.DbSupport;

@ApplicationScoped
@Slf4j
public class AssetService {

  @Inject DbSupport dbSupport;

  public List<Asset> getAllAssets() {
    return dbSupport.getJdbi().withExtension(AssetDao.class, AssetDao::getAllAssets);
  }

  public List<Asset> getAssetsForProvider(String providerId) {
    return dbSupport.getJdbi().withExtension(AssetDao.class, dao -> dao.getAssets(providerId));
  }

  public List<Asset> getAssetsForMissionMultiple(List<Mission> missions) {
    final var tuples =
        missions.stream()
            .map(a -> String.format("('%s', '%s')", a.providerId, a.assetId))
            .collect(Collectors.toList());

    var sql =
        """
      select * from assets
      where (provider_id, asset_id) in (<tuples>)
      """;

    var res =
        dbSupport
            .getJdbi()
            .withHandle(
                handle ->
                    handle
                        .createQuery(sql)
                        .defineList("tuples", tuples)
                        .mapToBean(Asset.class)
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
                      ac -> String.format("('%s', '%s')", ac.providerId, ac.assetId),
                      Collectors.toList()));
      // 2) then, map tuples to corresponding entity:
      // (note that each map value is necessarily non-empty per the above grouping)
      res = tuples.stream().map(p -> byTupleId.get(p).get(0)).collect(Collectors.toList());
    }
    return res;
  }

  public Map<String, List<Asset>> getAssetsMultiple(List<String> providerIds) {
    return dbSupport
        .getJdbi()
        .withExtension(
            AssetDao.class,
            dao -> {
              var assets = dao.getAssetsMultiple(providerIds);

              return assets.stream()
                  .collect(Collectors.groupingBy(Asset::getProviderId, Collectors.toList()));
            });
  }

  public Asset getAsset(String providerId, String assetId) {
    return dbSupport
        .getJdbi()
        .withExtension(AssetDao.class, dao -> dao.getAsset(providerId, assetId));
  }

  public Asset createAsset(Asset pl) {
    return dbSupport.getJdbi().withExtension(AssetDao.class, dao -> dao.insertAsset(pl));
  }

  public Asset updateAsset(Asset pl) {
    log.debug("updateAsset: pl={}", pl);
    return doUpdate(pl);
  }

  private Asset doUpdate(Asset pl) {
    var uDef =
        DbUtl.updateDef("assets", pl)
            .where("providerId")
            .where("assetId")
            .set(pl.className, "className")
            .set(pl.description, "description");

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
                          return rs.next() ? AssetMapper.instance.map(rs, ctx) : null;
                        }));
  }

  public Asset deleteAsset(Asset pl) {
    return dbSupport
        .getJdbi()
        .withExtension(AssetDao.class, dao -> dao.deleteAsset(pl.providerId, pl.assetId));
  }

  public List<List<Asset>> getAssetsForProviderIds(List<String> providerIds) {
    var byProviderId = getAssetsMultiple(providerIds);
    var res = new ArrayList<List<Asset>>();

    for (String providerId : providerIds) {
      res.add(byProviderId.get(providerId));
    }
    return res;
  }
}
