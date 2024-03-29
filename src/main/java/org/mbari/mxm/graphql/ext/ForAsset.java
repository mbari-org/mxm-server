package org.mbari.mxm.graphql.ext;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.microprofile.graphql.Source;
import org.mbari.mxm.db.asset.Asset;
import org.mbari.mxm.db.assetClass.AssetClass;
import org.mbari.mxm.db.assetClass.AssetClassService;

@ApplicationScoped
@RegisterForReflection
@Unremovable
public class ForAsset {
  @Inject AssetClassService assetClassService;

  public List<AssetClass> assetClass(@Source List<Asset> assets) {
    return assetClassService.getAssetClasses(assets);
  }
}
