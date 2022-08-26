package org.mbari.mxm.graphql;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.graphql.Source;
import org.mbari.mxm.db.argument.Argument;
import org.mbari.mxm.db.argument.ArgumentService;
import org.mbari.mxm.db.asset.Asset;
import org.mbari.mxm.db.asset.AssetService;
import org.mbari.mxm.db.mission.Mission;
import org.mbari.mxm.db.missionTemplate.MissionTemplate;
import org.mbari.mxm.db.missionTemplate.MissionTemplateService;
import org.mbari.mxm.db.provider.Provider;
import org.mbari.mxm.db.provider.ProviderService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;

@ApplicationScoped
@RegisterForReflection
@Unremovable
public class ForMission {
  @Inject
  ProviderService providerService;

  @Inject
  MissionTemplateService missionTemplateService;

  @Inject
  AssetService assetService;

  @Inject
  ArgumentService argumentService;

  public List<Provider> provider(@Source List<Mission> missions) {
    List<String> providerIds = missions.stream().map(e -> e.providerId).collect(toList());
    return providerService.getProviders(providerIds);
  }

  public List<MissionTemplate> missionTemplate(@Source List<Mission> missions) {
    return missionTemplateService.getMissionTemplates(missions);
  }

  public List<Asset> asset(@Source List<Mission> missions) {
    return assetService.getAssetsForMissionMultiple(missions);
  }

  public List<List<Argument>> arguments(@Source List<Mission> missions) {
    return argumentService.getArgumentsMultiple(missions);
  }

}
