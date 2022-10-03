package org.mbari.mxm.graphql;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.graphql.Source;
import org.mbari.mxm.db.asset.Asset;
import org.mbari.mxm.db.asset.AssetService;
import org.mbari.mxm.db.assetClass.AssetClass;

@ApplicationScoped
@RegisterForReflection
@Unremovable
public class ForAssetClass {
  @Inject AssetService assetService;

  public List<List<Asset>> assets(@Source List<AssetClass> assetClasses) {
    return assetService.getAssetsMultipleForAssetClasses(assetClasses);
  }
}
