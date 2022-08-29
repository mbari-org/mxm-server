package org.mbari.mxm;

import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.db.argument.ArgumentService;
import org.mbari.mxm.db.asset.Asset;
import org.mbari.mxm.db.asset.AssetService;
import org.mbari.mxm.db.assetClass.AssetClass;
import org.mbari.mxm.db.assetClass.AssetClassService;
import org.mbari.mxm.db.mission.Mission;
import org.mbari.mxm.db.mission.MissionService;
import org.mbari.mxm.db.mission.MissionStatusType;
import org.mbari.mxm.db.missionTemplate.MissionTemplate;
import org.mbari.mxm.db.missionTemplate.MissionTemplateService;
import org.mbari.mxm.db.missionTemplateAssetClass.MissionTemplateAssetClass;
import org.mbari.mxm.db.missionTemplateAssetClass.MissionTemplateAssetClassService;
import org.mbari.mxm.db.parameter.Parameter;
import org.mbari.mxm.db.parameter.ParameterService;
import org.mbari.mxm.db.provider.Provider;
import org.mbari.mxm.db.provider.ProviderApiType;
import org.mbari.mxm.db.unit.Unit;
import org.mbari.mxm.db.unit.UnitService;
import org.mbari.mxm.graphql.ProviderPingException;
import org.mbari.mxm.provider_client.MxmProviderClient;
import org.mbari.mxm.provider_client.MxmProviderClientBuilder;
import org.mbari.mxm.provider_client.responses.AssetClassesResponse;
import org.mbari.mxm.provider_client.responses.MissionTemplateResponse;
import org.mbari.mxm.provider_client.responses.PingResponse;
import org.mbari.mxm.provider_client.responses.UnitsResponse;
import org.mbari.mxm.provider_client.rest.MissionPayload;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class ProviderManager {

  @Inject
  AssetClassService assetClassService;

  @Inject
  AssetService assetService;

  @Inject
  UnitService unitService;

  @Inject
  MissionTemplateService missionTemplateService;

  @Inject
  MissionTemplateAssetClassService missionTemplateAssetClassService;

  @Inject
  ParameterService parameterService;

  @Inject
  MissionService missionService;

  @Inject
  ArgumentService argumentService;

  public PMInstance createInstance(String providerId,
                                   String httpEndpoint,
                                   ProviderApiType apiType) {

    var instance = new PMInstance();
    instance.setMxmProviderClient(providerId, httpEndpoint, apiType);
    return instance;

  }

  public class PMInstance {

    private MxmProviderClient mxmProviderClient;

    public void setMxmProviderClient(String providerId,
                                     String httpEndpoint,
                                     ProviderApiType apiType) {
      log.debug("setMxmProviderClient: providerId={}, httpEndpoint={}, apiType={}", providerId, httpEndpoint, apiType);
      mxmProviderClient = MxmProviderClientBuilder.create(providerId, httpEndpoint, apiType);
    }

    public PingResponse ping() throws ProviderPingException {
      return mxmProviderClient.ping();
    }

    public void preInsertProvider(Provider provider) {
      log.debug("preInsertProvider: provider={}", provider);

      // TODO this try/catch is mainly for the TFT@TSAUV provider, which doesn't support `ping` yet
      try {
        var pong = mxmProviderClient.ping();
        log.debug("preInsertProvider: ping=>{}", pong);
      }
      catch (ProviderPingException e) {
        log.warn("preInsertProvider: error pinging provider={}: {}", provider.providerId, e.getMessage());
      }

      var infoResponse = mxmProviderClient.getGeneralInfo();
      var info = infoResponse.result;
      log.debug("preInsertProvider: info=>{}", info);

      provider.description = info.providerDescription;
      provider.descriptionFormat = info.descriptionFormat;

      var caps = info.capabilities;
      provider.usesSched = caps.usesSched;
      provider.usesUnits = caps.usesUnits;
      provider.canValidate = caps.canValidate;
      provider.canReportMissionStatus = caps.canReportMissionStatus;
    }

    public void postInsertProvider(Provider provider) {
      log.debug("postInsertProvider: provider={}", provider);

      // Asset Classes
      var assetClasses = mxmProviderClient.getAssetClasses();
      log.debug("postInsertProvider: assetClasses=>{}", assetClasses);
      createAssetClasses(provider, assetClasses.result);

      // Units:
      if (provider.usesUnits) {
        getAndCreateUnits(provider);
      }

      // MissionTpls:
      getAndCreateMissionTplsForDirectory(provider, "/");
    }

    private void createAssetClasses(Provider provider, List<AssetClassesResponse.AssetClass> assetClasses) {
      assetClasses.forEach(assetClass -> {
        createAssetClass(provider, assetClass);
      });
    }

    private void createAssetClass(Provider provider, AssetClassesResponse.AssetClass assetClass) {
      log.debug("createAssetClass: assetClass=>{}", assetClass);
      var ac = assetClassService.createAssetClass(new AssetClass(provider.providerId, assetClass.assetClassName,
        assetClass.description));
      log.debug("createAssetClass: created=>{}", ac);

      createAssets(provider, assetClass);
    }

    private void createAssets(Provider provider, AssetClassesResponse.AssetClass assetClass) {
      assetClass.assets.forEach(asset -> {
        createAsset(provider, assetClass, asset);
      });
    }

    private void createAsset(Provider provider, AssetClassesResponse.AssetClass assetClass, AssetClassesResponse.Asset a) {
      log.debug("createAsset: a=>{}", a);
      var asset = new Asset(provider.providerId, a.assetId);
      asset.className = assetClass.assetClassName;
      // TODO capture description in AssetClassesResponse.Asset
      assetService.createAsset(asset);
    }

    private void getAndCreateMissionTplsForDirectory(Provider provider, String directory) {
      assert directory.endsWith("/");

      // create a MissionTemplate entry for the directory itself:
      getAndCreateMissionTpl(provider, directory);

      // get all directory entries, recursively as specified in MXM Provider API:
      var missionTplListing = mxmProviderClient.getMissionTemplates(
        directory.replaceFirst("^/+", "")   // TODO consistent path name handling
      );
      createMissionTplsForDirecvtoryEntries(provider, missionTplListing.result.entries);
    }

    private void createMissionTplsForDirecvtoryEntries(Provider provider,
                                                       List<MissionTemplateResponse.MissionTemplate> entries) {
      entries.forEach(entry -> {
        final var missionTplId = Utl.cleanPath(entry.missionTplId);
        final var isDirectory = missionTplId.endsWith("/");

        MissionTemplate missionTemplate = new MissionTemplate(provider.providerId, missionTplId);
        missionTemplate.description = entry.description;
        missionTemplate.retrievedAt = isDirectory ? OffsetDateTime.now() : null;

        // create this entry:
        missionTemplateService.createMissionTemplate(missionTemplate);

        if (isDirectory) {
          if (entry.entries != null && entry.entries.size() > 0) {
            createMissionTplsForDirecvtoryEntries(provider, entry.entries);
          }
        }
        else {
          // just add the associated asset classes to the mission template:
          if (entry.assetClassNames != null) {
            createAssociatedAssetClasses(provider, missionTemplate, entry.assetClassNames);
          }
        }
      });
    }

    private void getAndCreateMissionTpl(Provider provider, String missionTplId) {
      missionTplId = Utl.cleanPath(missionTplId);
      final var isDirectory = missionTplId.endsWith("/");

      MissionTemplate missionTemplate = new MissionTemplate(provider.providerId, missionTplId);
      missionTemplate.retrievedAt = OffsetDateTime.now();

      if (isDirectory) {
        // no info needed from provider, just create the entry:
        missionTemplateService.createMissionTemplate(missionTemplate);
      }
      else {
        // actual template, get info from provider as needed:
        log.debug("getAndCreateMissionTpl: missionTplId='{}'", missionTplId);
        var response = mxmProviderClient.getMissionTemplate(missionTplId);
        var missionTemplateFromProvider = response.result;
        createActualMissionTemplate(provider, missionTemplate, missionTemplateFromProvider);
      }
    }

    private void createActualMissionTemplate(Provider provider, MissionTemplate missionTemplate,
                                             MissionTemplateResponse.MissionTemplate missionTemplateFromProvider
    ) {
      missionTemplate.description = missionTemplateFromProvider.description;
      missionTemplate.retrievedAt = OffsetDateTime.now();

      missionTemplateService.createMissionTemplate(missionTemplate);

      if (missionTemplateFromProvider.assetClassNames != null) {
        createAssociatedAssetClasses(provider, missionTemplate, missionTemplateFromProvider.assetClassNames);
      }

      if (missionTemplateFromProvider.parameters != null) {
        createParameters(provider, missionTemplate, missionTemplateFromProvider.parameters);
      }
    }

    private void createAssociatedAssetClasses(Provider provider, MissionTemplate missionTemplate,
                                              List<String> assetClassNames) {

      assetClassNames.forEach(assetClassName -> {
        missionTemplateAssetClassService.createMissionTemplateAssetClass(
          new MissionTemplateAssetClass(provider.providerId, missionTemplate.missionTplId, assetClassName)
        );
      });
    }

    private void createParameters(Provider provider, MissionTemplate missionTemplate,
                                  List<MissionTemplateResponse.Parameter> parameters
    ) {
      parameters.forEach(pp -> {
        var parameter = new Parameter(provider.providerId, missionTemplate.missionTplId, pp.paramName);
        parameter.type = pp.type;
        parameter.required = pp.required;
        parameter.defaultValue = pp.defaultValue;
        parameter.defaultUnits = pp.defaultUnits;
        parameter.valueCanReference = pp.valueCanReference;
        parameter.description = pp.description;
        parameterService.createParameter(parameter);
      });
    }


    private void getAndCreateUnits(Provider provider) {
      var units = mxmProviderClient.getUnits().result.stream()
        .collect(Collectors.partitioningBy(u -> u.baseUnit == null));

      // first, create units without a base:
      units.get(true).forEach(u -> {
        createUnit(provider, u);
      });
      // then the others:
      units.get(false).forEach(u -> {
        createUnit(provider, u);
      });
    }

    private void createUnit(Provider provider, UnitsResponse.Unit unit) {
      var u = new Unit(provider.providerId, unit.name);
      u.abbreviation = unit.abbreviation;
      u.baseUnit = unit.baseUnit;
      var created = unitService.createUnit(u);
      log.trace("createUnit: created=>{}", created);
    }


    /**
     * Reloads mission template from the provider.
     */
    public void preUpdateMissionTpl(Provider provider, MissionTemplate pl) {
      log.debug("preUpdateMissionTpl: missionTemplate={}", pl);

      final var missionTplId = pl.missionTplId;

      if (missionTplId.endsWith("/")) {
        var deleteAllRes = missionTemplateService.deleteDirectoryRecursive(provider.providerId, missionTplId);
        log.debug("preUpdateMissionTpl: deleteAllRes=>{}", deleteAllRes);

        log.debug("recreating missionTplId='{}'", missionTplId);
        getAndCreateMissionTplsForDirectory(provider, missionTplId);
      }
      else {
        missionTemplateService.deleteMissionTemplate(pl);
        log.debug("recreating template missionTplId='{}'", missionTplId);
        getAndCreateMissionTpl(provider, missionTplId);
      }
    }

    public void preUpdateMission(Provider provider, Mission pl) {
      log.debug("preUpdateMission: pl={}", pl);

      // get the current state of the mission:
      var mission = missionService.getMission(pl.providerId, pl.missionTplId, pl.missionId);

      // depending on current missionStatus:
      if (mission.missionStatus == MissionStatusType.DRAFT) {
        // is mission being submitted?
        if (pl.missionStatus == MissionStatusType.SUBMITTED) {
          log.warn("preUpdateMission: submitting, pl={}", Utl.writeJson(pl));
          submitMission(provider, mission);
        }
        else if (pl.missionStatus == null || pl.missionStatus == MissionStatusType.DRAFT) {
          // OK, no requested change in status; let mutation proceed.
          pl.setUpdatedDate(OffsetDateTime.now());
        }
        else {
          // from DRAFT, only SUBMITTED is allowed.
          throw new Error(String.format("Unexpected pl.missionStatus=%s in DRAFT status",
            pl.missionStatus));
        }
      }
      else if (pl.noPatch()) {
        // This is a request for refreshing the mission status.
        if (provider.canReportMissionStatus) {
          pl.missionStatus = retrieveMissionStatus(mission);
          pl.setUpdatedDate(OffsetDateTime.now());
        }
        else {
          log.warn("provider '{}' does not support reporting mission status", provider.providerId);
        }
      }
      else {
        throw new IllegalStateException("Unexpected pl.missionStatus: " + pl.missionStatus);
      }
    }

    private void submitMission(Provider provider, Mission mission) {
      var args= argumentService.getArguments(mission.providerId, mission.missionTplId, mission.missionId);

      MissionPayload pl = new MissionPayload();

      pl.arguments = new HashMap<>();
      args.forEach(a -> {
        pl.arguments.put(a.paramName, new MissionPayload.MissionArgValueAndUnits(a.paramValue, a.paramUnits));
      });

      pl.missionTplId = mission.missionTplId;
      pl.missionId = mission.missionId;
      pl.assetId = mission.assetId;
      pl.description = mission.description;
      pl.schedType = mission.schedType == null ? null : mission.schedType.name();
      pl.schedDate = mission.schedDate == null ? null : mission.schedDate.toString();

      log.warn("submitMission: pl={}", Utl.writeJson(pl));
      var res = mxmProviderClient.postMission(pl);
      log.warn("submitMission: res={}", Utl.writeJson(res));
      if (res.result.status == null) {
        log.warn("submitMission: res.result.status is null");
      }
    }

    private MissionStatusType retrieveMissionStatus(Mission mission) {
      // TODO

      //mxmProviderClient.getMission(mission.missionId);
      return null;
    }

    public void done() {
      mxmProviderClient.done();
    }
  }
}
