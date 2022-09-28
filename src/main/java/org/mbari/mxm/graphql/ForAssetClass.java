package org.mbari.mxm.graphql;

import static java.util.stream.Collectors.toList;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.graphql.Source;
import org.mbari.mxm.db.asset.Asset;
import org.mbari.mxm.db.asset.AssetService;
import org.mbari.mxm.db.assetClass.AssetClass;
import org.mbari.mxm.db.provider.Provider;
import org.mbari.mxm.db.provider.ProviderService;

@ApplicationScoped
@RegisterForReflection
@Unremovable
public class ForAssetClass {
  @Inject AssetService assetService;

  @Inject ProviderService providerService;

  public List<List<Asset>> assets(@Source List<AssetClass> assetClasses) {
    return assetService.getAssetsMultipleForAssetClasses(assetClasses);
  }

  public List<Provider> provider(@Source List<AssetClass> assetClasses) {
    List<String> providerIds = assetClasses.stream().map(e -> e.providerId).collect(toList());
    return providerService.getProviders(providerIds);
  }
}
