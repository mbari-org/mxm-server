package org.mbari.mxm.db.assetClass;

import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.db.DbUtl;
import org.mbari.mxm.db.asset.Asset;
import org.mbari.mxm.db.support.DbSupport;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class AssetClassService {

  @Inject
  DbSupport dbSupport;

  public List<AssetClass> getAllAssetClasses() {
    return dbSupport.getJdbi()
      .withExtension(AssetClassDao.class, AssetClassDao::getAllAssetClasses);
  }

  public List<AssetClass> getAssetClasses(String providerId) {
    return dbSupport.getJdbi()
      .withExtension(AssetClassDao.class, dao -> dao.getAssetClasses(providerId));
  }

  public Map<String, List<AssetClass>> getAssetClassesMultiple(List<String> providerIds) {
    return dbSupport.getJdbi()
      .withExtension(AssetClassDao.class, dao -> {
        var assetClasses = dao.getAssetClassesMultiple(providerIds);

        return assetClasses.stream().collect(
          Collectors.groupingBy(AssetClass::getProviderId, Collectors.toList()));
      });
  }

  public AssetClass getAssetClass(String providerId, String className) {
    return dbSupport.getJdbi()
      .withExtension(AssetClassDao.class, dao -> dao.getAssetClass(providerId, className));
  }

  public List<AssetClass> getAssetClasses(List<Asset> assets) {
    // Note: there could be multiple assets with the same class,
    // so, a direct "mapping" would cause the following in such cases:
    //
    // SRGQL012000: Data Fetching Error:
    //    org.dataloader.impl.DataLoaderAssertionException:
    //    The size of the promised values MUST be the same size as the key list
    //

    final var tuples = assets.stream()
      .map(a -> String.format("('%s', '%s')", a.providerId, a.className))
      .collect(Collectors.toList());

    // we could probably simplify to use actual different keys for the query below,
    // but let's pass the whole list for now
    var sql = """
      select * from asset_classes
      where (provider_id, class_name) in (<tuples>)
      """;

    var res = dbSupport.getJdbi()
      .withHandle(handle ->
        handle.createQuery(sql)
          .defineList("tuples", tuples)
          .mapToBean(AssetClass.class)
          .list()
      );

    log.debug("tuples({}) = {}", tuples.size(), tuples);
    log.debug("res({}) = {}", res.size(), res);

    if (tuples.size() != res.size()) {
      // need to "align" for the result.
      // 1) get a map to resolve AssetClass by the "tuple id":
      var byTupleId = res.stream().collect(
        Collectors.groupingBy(
          ac -> String.format("('%s', '%s')", ac.providerId, ac.className),
          Collectors.toList())
      );
      // 2) then, map tuples to corresponding AssetClasses:
      // (note that each map value is necessarily non-empty per the above grouping)
      res = tuples.stream().map(p -> byTupleId.get(p).get(0)).collect(Collectors.toList());
    }
    return res;
  }

  public AssetClass createAssetClass(AssetClass pl) {
    return dbSupport.getJdbi()
      .withExtension(AssetClassDao.class,
        dao -> dao.insertAssetClass(pl)
      );
  }

  private final AssetClassMapper assetClassMapper = new AssetClassMapper();

  public AssetClass updateAssetClass(AssetClass pl) {
    log.debug("updateAssetClass: pl={}", pl);
    return doUpdate(pl);
  }

  private AssetClass doUpdate(AssetClass pl) {
    var uDef = DbUtl.updateDef("assets", pl)
      .where("providerId")
      .where("className")
      .set(pl.description, "description");

    if (uDef.noSets()) {
      return pl;
    }
    return dbSupport.getJdbi()
      .withHandle(handle ->
        uDef.createQuery(handle)
          .execute((statementSupplier, ctx) -> {
            var rs = statementSupplier.get().executeQuery();
            return rs.next() ? AssetClassMapper.instance.map(rs, ctx) : null;
          })
      );
  }

  public AssetClass deleteAssetClass(AssetClass pl) {
    return dbSupport.getJdbi()
      .withExtension(AssetClassDao.class,
        dao -> dao.deleteAssetClass(pl.providerId, pl.className)
      );
  }
}

