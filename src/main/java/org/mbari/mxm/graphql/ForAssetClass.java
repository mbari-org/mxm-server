package org.mbari.mxm.graphql;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.graphql.Source;
import org.mbari.mxm.db.asset.Asset;
import org.mbari.mxm.db.asset.AssetService;
import org.mbari.mxm.db.assetClass.AssetClass;
import org.mbari.mxm.db.provider.Provider;
import org.mbari.mxm.db.provider.ProviderService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;

@ApplicationScoped
@RegisterForReflection
@Unremovable
public class ForAssetClass {
  @Inject
  AssetService assetService;

  @Inject
  ProviderService providerService;

  public List<List<Asset>> assets(@Source List<AssetClass> assetClasses) {
    List<String> providerIds = assetClasses.stream().map(e -> e.providerId).collect(toList());
    return assetService.getAssetsForProviderIds(providerIds);
  }

  public List<Provider> provider(@Source List<AssetClass> assetClasses) {
    List<String> providerIds = assetClasses.stream().map(e -> e.providerId).collect(toList());
    return providerService.getProviders(providerIds);
  }

}
