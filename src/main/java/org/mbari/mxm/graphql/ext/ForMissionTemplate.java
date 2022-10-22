package org.mbari.mxm.graphql.ext;

import static java.util.stream.Collectors.toList;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.graphql.Source;
import org.mbari.mxm.db.assetClass.AssetClass;
import org.mbari.mxm.db.mission.Mission;
import org.mbari.mxm.db.mission.MissionService;
import org.mbari.mxm.db.missionTemplate.MissionTemplate;
import org.mbari.mxm.db.missionTemplateAssetClass.MissionTemplateAssetClassService;
import org.mbari.mxm.db.parameter.Parameter;
import org.mbari.mxm.db.parameter.ParameterService;
import org.mbari.mxm.db.provider.Provider;
import org.mbari.mxm.db.provider.ProviderService;

@ApplicationScoped
@RegisterForReflection
@Unremovable
public class ForMissionTemplate {
  @Inject ProviderService providerService;

  @Inject ParameterService parameterService;

  @Inject MissionService missionService;

  @Inject MissionTemplateAssetClassService missionTemplateAssetClassService;

  public List<Provider> provider(@Source List<MissionTemplate> missionTemplates) {
    List<String> providerIds = missionTemplates.stream().map(e -> e.providerId).collect(toList());
    return providerService.getProviders(providerIds);
  }

  public List<List<Parameter>> parameters(@Source List<MissionTemplate> missionTemplates) {
    return parameterService.getParametersMultiple(missionTemplates);
  }

  public List<List<Mission>> missions(@Source List<MissionTemplate> missionTemplates) {
    return missionService.getMissionsMultiple(missionTemplates);
  }

  public List<List<AssetClass>> assetClasses(@Source List<MissionTemplate> missionTemplates) {
    return missionTemplateAssetClassService.getAssetClassesMultiple(missionTemplates);
  }
}
