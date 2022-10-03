package org.mbari.mxm.graphql;

import static java.util.stream.Collectors.toList;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Source;
import org.mbari.mxm.db.assetClass.AssetClass;
import org.mbari.mxm.db.mission.Mission;
import org.mbari.mxm.db.mission.MissionService;
import org.mbari.mxm.db.missionTemplate.MissionTemplate;
import org.mbari.mxm.db.missionTemplate.MissionTemplateService;
import org.mbari.mxm.db.missionTemplateAssetClass.MissionTemplateAssetClassService;
import org.mbari.mxm.db.provider.Provider;

@ApplicationScoped
@RegisterForReflection
@Unremovable
public class ForProvider {
  @Inject MissionTemplateService missionTemplateService;

  @Inject MissionTemplateAssetClassService missionTemplateAssetClassService;

  @Inject MissionService missionService;

  public List<List<MissionTemplate>> missionTemplates(@Source List<Provider> providers) {
    List<String> providerIds = providers.stream().map(e -> e.providerId).collect(toList());
    var byProviderId = missionTemplateService.getMissionTemplatesMultiple(providerIds);
    var res = new ArrayList<List<MissionTemplate>>();
    for (String providerId : providerIds) {
      res.add(byProviderId.get(providerId));
    }
    return res;
  }

  @Description("Number of actual mission templates (directories excluded)")
  public List<Integer> numActualMissionTemplates(@Source List<Provider> providers) {
    return missionTemplates(providers).stream()
        .map(l -> l == null ? 0 : l.stream().filter(mt -> !mt.isDirectory()).toList().size())
        .collect(toList());
  }

  public List<List<Mission>> missions(@Source List<Provider> providers) {
    List<String> providerIds = providers.stream().map(e -> e.providerId).collect(toList());
    var byProviderId = missionService.getMissionsForProviderMultiple(providerIds);
    var res = new ArrayList<List<Mission>>();
    for (String providerId : providerIds) {
      res.add(byProviderId.get(providerId));
    }
    return res;
  }

  public List<Integer> numMissions(@Source List<Provider> providers) {
    return missions(providers).stream().map(this::listSize).collect(toList());
  }

  @Description("Asset classes used by this provider's mission templates")
  public List<List<AssetClass>> assetClasses(@Source List<Provider> providers) {
    final var providerIds =
        providers.stream()
            .map(e -> String.format("'%s'", e.providerId))
            .collect(Collectors.toList());
    return missionTemplateAssetClassService.getAssetClassesMultipleProviders(providerIds);
  }

  public List<Integer> numAssetClasses(@Source List<Provider> providers) {
    return assetClasses(providers).stream().map(this::listSize).collect(toList());
  }

  private <T> int listSize(List<T> list) {
    return list != null ? list.size() : 0;
  }
}
