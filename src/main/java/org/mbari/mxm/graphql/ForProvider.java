package org.mbari.mxm.graphql;

import io.quarkus.arc.Unremovable;
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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@ApplicationScoped
@Unremovable
public class ForProvider {
  @Inject
  AssetClassService assetClassService;

  @Inject
  AssetService assetService;

  @Inject
  MissionTemplateService missionTemplateService;

  @Inject
  MissionService missionService;

  @Inject
  UnitService unitService;

  public List<List<AssetClass>> assetClasses(@Source List<Provider> providers) {
    List<String> providerIds = providers.stream().map(e -> e.providerId).collect(toList());
    var byProviderId = assetClassService.getAssetClassesMultiple(providerIds);
    var res = new ArrayList<List<AssetClass>>();
    for (String providerId : providerIds) {
      res.add(byProviderId.get(providerId));
    }
    return res;
  }

  public List<List<Asset>> assets(@Source List<Provider> providers) {
    List<String> providerIds = providers.stream().map(e -> e.providerId).collect(toList());
    return assetService.getAssetsForProviderIds(providerIds);
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

  public List<List<Mission>> missions(@Source List<Provider> providers) {
    List<String> providerIds = providers.stream().map(e -> e.providerId).collect(toList());
    var byProviderId = missionService.getMissionsForProviderMultiple(providerIds);
    var res = new ArrayList<List<Mission>>();
    for (String providerId : providerIds) {
      res.add(byProviderId.get(providerId));
    }
    return res;
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
}
