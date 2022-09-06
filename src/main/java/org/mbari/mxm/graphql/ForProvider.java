package org.mbari.mxm.graphql;

import static java.util.stream.Collectors.toList;

import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.Source;
import org.mbari.mxm.db.asset.Asset;
import org.mbari.mxm.db.asset.AssetService;
import org.mbari.mxm.db.assetClass.AssetClass;
import org.mbari.mxm.db.assetClass.AssetClassService;
import org.mbari.mxm.db.mission.Mission;
import org.mbari.mxm.db.mission.MissionService;
import org.mbari.mxm.db.missionTemplate.MissionTemplate;
import org.mbari.mxm.db.missionTemplate.MissionTemplateService;
import org.mbari.mxm.db.provider.Provider;
import org.mbari.mxm.db.unit.Unit;
import org.mbari.mxm.db.unit.UnitService;

@ApplicationScoped
@RegisterForReflection
@Unremovable
public class ForProvider {
  @Inject AssetClassService assetClassService;

  @Inject AssetService assetService;

  @Inject MissionTemplateService missionTemplateService;

  @Inject MissionService missionService;

  @Inject UnitService unitService;

  public List<List<AssetClass>> assetClasses(@Source List<Provider> providers) {
    List<String> providerIds = providers.stream().map(e -> e.providerId).collect(toList());
    var byProviderId = assetClassService.getAssetClassesMultiple(providerIds);
    var res = new ArrayList<List<AssetClass>>();
    for (String providerId : providerIds) {
      res.add(byProviderId.get(providerId));
    }
    return res;
  }

  public List<Integer> numAssetClasses(@Source List<Provider> providers) {
    return assetClasses(providers).stream().map(this::listSize).collect(toList());
  }

  public List<List<Asset>> assets(@Source List<Provider> providers) {
    List<String> providerIds = providers.stream().map(e -> e.providerId).collect(toList());
    return assetService.getAssetsForProviderIds(providerIds);
  }

  public List<Integer> numAssets(@Source List<Provider> providers) {
    return assets(providers).stream().map(this::listSize).collect(toList());
  }

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

  public List<List<Unit>> units(@Source List<Provider> providers) {
    List<String> providerIds = providers.stream().map(e -> e.providerId).collect(toList());
    var byProviderId = unitService.getUnitsMultiple(providerIds);
    var res = new ArrayList<List<Unit>>();
    for (String providerId : providerIds) {
      res.add(byProviderId.get(providerId));
    }
    return res;
  }

  public List<Integer> numUnits(@Source List<Provider> providers) {
    return units(providers).stream().map(this::listSize).collect(toList());
  }

  private <T> int listSize(List<T> list) {
    return list != null ? list.size() : 0;
  }
}
