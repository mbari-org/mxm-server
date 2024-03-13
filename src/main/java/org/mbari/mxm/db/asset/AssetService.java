package org.mbari.mxm.db.asset;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.db.DbUtl;
import org.mbari.mxm.db.assetClass.AssetClass;
import org.mbari.mxm.db.mission.Mission;
import org.mbari.mxm.db.support.DbSupport;

@ApplicationScoped
@Slf4j
public class AssetService {

  @Inject DbSupport dbSupport;

  public List<Asset> getAllAssets() {
    return dbSupport.getJdbi().withExtension(AssetDao.class, AssetDao::getAllAssets);
  }

  public List<Asset> getAssetsForMissionMultiple(List<Mission> missions) {
    final var assetIds =
        missions.stream().map(a -> String.format("'%s'", a.assetId)).collect(Collectors.toList());

    var sql = """
      select * from assets
      where asset_id in (<assetIds>)
      """;

    var res =
        dbSupport
            .getJdbi()
            .withHandle(
                handle ->
                    handle
                        .createQuery(sql)
                        .defineList("assetIds", assetIds)
                        .mapToBean(Asset.class)
                        .list());

    var byAssetId =
        res.stream()
            .collect(
                Collectors.groupingBy(
                    ac -> String.format("'%s'", ac.assetId), Collectors.toList()));

    return assetIds.stream().map(p -> byAssetId.get(p).get(0)).collect(Collectors.toList());
  }

  public List<List<Asset>> getAssetsMultipleForAssetClasses(List<AssetClass> assetClasses) {

    final var classNames =
        assetClasses.stream()
            .map(e -> String.format("'%s'", e.className))
            .collect(Collectors.toList());

    var sql =
        """
    select * from assets
    where class_name in (<classNames>)
    order by asset_id
    """;

    var flatList =
        dbSupport
            .getJdbi()
            .withHandle(
                handle ->
                    handle
                        .createQuery(sql)
                        .defineList("classNames", classNames)
                        .mapToBean(Asset.class)
                        .list());

    var byClassName =
        flatList.stream()
            .collect(
                Collectors.groupingBy(
                    e -> String.format("'%s'", e.className), Collectors.toList()));

    return classNames.stream().map(byClassName::get).collect(Collectors.toList());
  }

  public Asset getAsset(String assetId) {
    return dbSupport.getJdbi().withExtension(AssetDao.class, dao -> dao.getAsset(assetId));
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

  public Asset deleteAsset(String assetId) {
    return dbSupport.getJdbi().withExtension(AssetDao.class, dao -> dao.deleteAsset(assetId));
  }
}
