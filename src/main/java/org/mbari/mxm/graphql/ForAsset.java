package org.mbari.mxm.graphql;

import io.quarkus.arc.Unremovable;
import org.eclipse.microprofile.graphql.Source;
import org.mbari.mxm.db.asset.Asset;
import org.mbari.mxm.db.assetClass.AssetClass;
import org.mbari.mxm.db.assetClass.AssetClassService;
import org.mbari.mxm.db.provider.Provider;
import org.mbari.mxm.db.provider.ProviderService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;

@ApplicationScoped
@Unremovable
public class ForAsset {
  @Inject
  AssetClassService assetClassService;

  @Inject
  ProviderService providerService;

  public List<AssetClass> assetClass(@Source List<Asset> assets) {
    return assetClassService.getAssetClasses(assets);
  }

  public List<Provider> provider(@Source List<Asset> assets) {
    List<String> providerIds = assets.stream().map(e -> e.providerId).collect(toList());
    return providerService.getProviders(providerIds);
  }
}
