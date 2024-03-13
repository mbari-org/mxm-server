package org.mbari.mxm.db.assetClass;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.db.DbUtl;
import org.mbari.mxm.db.asset.Asset;
import org.mbari.mxm.db.support.DbSupport;

@ApplicationScoped
@Slf4j
public class AssetClassService {

  @Inject DbSupport dbSupport;

  public List<AssetClass> getAllAssetClasses() {
    return dbSupport
        .getJdbi()
        .withExtension(AssetClassDao.class, AssetClassDao::getAllAssetClasses);
  }

  public AssetClass getAssetClass(String className) {
    return dbSupport
        .getJdbi()
        .withExtension(AssetClassDao.class, dao -> dao.getAssetClass(className));
  }

  public List<AssetClass> getAssetClasses(List<Asset> assets) {
    final var classNames =
        assets.stream()
            .map(a -> String.format("'%s'", a.className))
            .collect(Collectors.toSet())
            .stream()
            .toList();

    var sql =
        """
      select * from asset_classes
      where class_name in (<classNames>)
      """;

    var assetClasses =
        dbSupport
            .getJdbi()
            .withHandle(
                handle ->
                    handle
                        .createQuery(sql)
                        .defineList("classNames", classNames)
                        .mapToBean(AssetClass.class)
                        .list());

    var byClassName = assetClasses.stream().collect(Collectors.toMap(ac -> ac.className, ac -> ac));
    return assets.stream().map(a -> byClassName.get(a.className)).collect(Collectors.toList());
  }

  public AssetClass createAssetClass(AssetClass pl) {
    return dbSupport.getJdbi().withExtension(AssetClassDao.class, dao -> dao.insertAssetClass(pl));
  }

  public AssetClass updateAssetClass(AssetClass pl) {
    log.debug("updateAssetClass: pl={}", pl);
    return doUpdate(pl);
  }

  private AssetClass doUpdate(AssetClass pl) {
    var uDef = DbUtl.updateDef("assets", pl).where("className").set(pl.description, "description");

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
                          return rs.next() ? AssetClassMapper.instance.map(rs, ctx) : null;
                        }));
  }

  public AssetClass deleteAssetClass(String className) {
    return dbSupport
        .getJdbi()
        .withExtension(AssetClassDao.class, dao -> dao.deleteAssetClass(className));
  }
}
