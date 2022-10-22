package org.mbari.mxm.graphql.ext;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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
