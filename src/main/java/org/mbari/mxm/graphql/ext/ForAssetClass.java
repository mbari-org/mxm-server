package org.mbari.mxm.graphql.ext;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Source;
import org.mbari.mxm.db.asset.Asset;
import org.mbari.mxm.db.asset.AssetService;
import org.mbari.mxm.db.assetClass.AssetClass;
import org.mbari.mxm.db.missionTemplate.MissionTemplate;
import org.mbari.mxm.db.missionTemplateAssetClass.MissionTemplateAssetClassService;
import org.mbari.mxm.db.provider.Provider;

@ApplicationScoped
@RegisterForReflection
@Unremovable
public class ForAssetClass {
  @Inject AssetService assetService;

  public List<List<Asset>> assets(@Source List<AssetClass> assetClasses) {
    return assetService.getAssetsMultipleForAssetClasses(assetClasses);
  }

  @Inject MissionTemplateAssetClassService missionTemplateAssetClassService;

  @Description("Get the mission templates that operate on assets of this class")
  public List<List<MissionTemplate>> missionTemplates(@Source List<AssetClass> assetClasses) {
    return missionTemplateAssetClassService.getMissionTemplatesMultiple(assetClasses);
  }

  @Description("Get the providers that operate on assets of this class")
  public List<List<Provider>> providers(@Source List<AssetClass> assetClasses) {
    return missionTemplateAssetClassService.getProvidersMultiple(assetClasses);
  }
}
