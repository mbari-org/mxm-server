package org.mbari.mxm;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.db.argument.Argument;
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
import org.mbari.mxm.provider_client.rest.PostMissionPayload;

@ApplicationScoped
@Slf4j
public class ProviderManager {

  @Inject AssetClassService assetClassService;

  @Inject AssetService assetService;

  @Inject UnitService unitService;

  @Inject MissionTemplateService missionTemplateService;

  @Inject MissionTemplateAssetClassService missionTemplateAssetClassService;

  @Inject ParameterService parameterService;

  @Inject MissionService missionService;

  @Inject ArgumentService argumentService;

  public PMInstance createInstance(
      String providerId, String httpEndpoint, ProviderApiType apiType) {

    var instance = new PMInstance();
    instance.setMxmProviderClient(providerId, httpEndpoint, apiType);
    return instance;
  }

  public class PMInstance {

    private MxmProviderClient mxmProviderClient;

    public void setMxmProviderClient(
        String providerId, String httpEndpoint, ProviderApiType apiType) {
      log.debug(
          "setMxmProviderClient: providerId={}, httpEndpoint={}, apiType={}",
          providerId,
          httpEndpoint,
          apiType);
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
      } catch (ProviderPingException e) {
        log.warn(
            "preInsertProvider: error pinging provider={}: {}",
            provider.providerId,
            e.getMessage());
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

    private void createAssetClasses(
        Provider provider, List<AssetClassesResponse.AssetClass> assetClasses) {
      assetClasses.forEach(assetClass -> createAssetClass(provider, assetClass));
    }

    private void createAssetClass(Provider provider, AssetClassesResponse.AssetClass assetClass) {
      log.debug("createAssetClass: assetClass=>{}", assetClass);
      var ac =
          assetClassService.createAssetClass(
              new AssetClass(
                  provider.providerId, assetClass.assetClassName, assetClass.description));
      log.debug("createAssetClass: created=>{}", ac);

      createAssets(provider, assetClass);
    }

    private void createAssets(Provider provider, AssetClassesResponse.AssetClass assetClass) {
      assetClass.assets.forEach(asset -> createAsset(provider, assetClass, asset));
    }

    private void createAsset(
        Provider provider,
        AssetClassesResponse.AssetClass assetClass,
        AssetClassesResponse.Asset a) {
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
      var missionTplListing =
          mxmProviderClient.getMissionTemplates(
              directory.replaceFirst("^/+", "") // TODO consistent path name handling
              );
      createMissionTplsForDirectoryEntries(provider, missionTplListing.result.entries);
    }

    private void createMissionTplsForDirectoryEntries(
        Provider provider, List<MissionTemplateResponse.MissionTemplate> entries) {
      entries.forEach(
          entry -> {
            final var missionTplId = Utl.cleanPath(entry.missionTplId);
            final var isDirectory = missionTplId.endsWith("/");

            MissionTemplate missionTemplate =
                new MissionTemplate(provider.providerId, missionTplId);
            missionTemplate.description = entry.description;
            missionTemplate.retrievedAt = isDirectory ? OffsetDateTime.now() : null;

            // create this entry:
            missionTemplateService.createMissionTemplate(missionTemplate);

            if (isDirectory) {
              if (entry.entries != null && entry.entries.size() > 0) {
                createMissionTplsForDirectoryEntries(provider, entry.entries);
              }
            } else {
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
      } else {
        // actual template, get info from provider as needed:
        log.debug("getAndCreateMissionTpl: missionTplId='{}'", missionTplId);
        var response = mxmProviderClient.getMissionTemplate(missionTplId);
        var missionTemplateFromProvider = response.result;
        createActualMissionTemplate(provider, missionTemplate, missionTemplateFromProvider);
      }
    }

    private void createActualMissionTemplate(
        Provider provider,
        MissionTemplate missionTemplate,
        MissionTemplateResponse.MissionTemplate missionTemplateFromProvider) {
      missionTemplate.description = missionTemplateFromProvider.description;
      missionTemplate.retrievedAt = OffsetDateTime.now();

      missionTemplateService.createMissionTemplate(missionTemplate);

      if (missionTemplateFromProvider.assetClassNames != null) {
        createAssociatedAssetClasses(
            provider, missionTemplate, missionTemplateFromProvider.assetClassNames);
      }

      if (missionTemplateFromProvider.parameters != null) {
        createParameters(provider, missionTemplate, missionTemplateFromProvider.parameters);
      }
    }

    private void createAssociatedAssetClasses(
        Provider provider, MissionTemplate missionTemplate, List<String> assetClassNames) {

      assetClassNames.forEach(
          assetClassName ->
              missionTemplateAssetClassService.createMissionTemplateAssetClass(
                  new MissionTemplateAssetClass(
                      provider.providerId, missionTemplate.missionTplId, assetClassName)));
    }

    private void createParameters(
        Provider provider,
        MissionTemplate missionTemplate,
        List<MissionTemplateResponse.Parameter> parameters) {
      parameters.forEach(
          pp -> {
            var parameter =
                new Parameter(provider.providerId, missionTemplate.missionTplId, pp.paramName);
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
      var units =
          mxmProviderClient.getUnits().result.stream()
              .collect(Collectors.partitioningBy(u -> u.baseUnit == null));

      // first, create units without a base:
      units.get(true).forEach(u -> createUnit(provider, u));
      // then the others:
      units.get(false).forEach(u -> createUnit(provider, u));
    }

    private void createUnit(Provider provider, UnitsResponse.Unit unit) {
      var u = new Unit(provider.providerId, unit.name);
      u.abbreviation = unit.abbreviation;
      u.baseUnit = unit.baseUnit;
      var created = unitService.createUnit(u);
      log.trace("createUnit: created=>{}", created);
    }

    /** Refreshes mission template information from the provider. */
    public void preUpdateMissionTpl(Provider provider, MissionTemplate pl) {
      log.debug("preUpdateMissionTpl: missionTemplate={}", pl);
      final var missionTplId = pl.missionTplId;
      if (missionTplId.endsWith("/")) {
        updateMissionTemplateDirectory(provider, missionTplId);
      } else {
        updateActualMissionTemplate(provider, missionTplId);
      }
    }

    private void updateMissionTemplateDirectory(Provider provider, String missionTplId) {
      // FIXME not a complete recreation, but a refresh depending on existing dependencies!
      var deleteAllRes =
          missionTemplateService.deleteDirectoryRecursive(provider.providerId, missionTplId);
      log.debug("preUpdateMissionTpl: deleteAllRes=>{}", deleteAllRes);

      log.debug("recreating missionTplId='{}'", missionTplId);
      getAndCreateMissionTplsForDirectory(provider, missionTplId);
    }

    private void updateActualMissionTemplate(Provider provider, String missionTplId) {
      log.debug("refreshing template missionTplId='{}'", missionTplId);
      var response = mxmProviderClient.getMissionTemplate(missionTplId);
      var missionTemplateFromProvider = response.result;

      // update mission template itself:
      MissionTemplate missionTemplate = new MissionTemplate(provider.providerId, missionTplId);
      missionTemplate.description = missionTemplateFromProvider.description;
      missionTemplate.retrievedAt = OffsetDateTime.now();
      missionTemplateService.updateMissionTemplate(missionTemplate);

      recreateAssociatedAssetClasses(
          provider, missionTplId, missionTemplate, missionTemplateFromProvider);

      refreshAssociatedParameters(provider, missionTplId, missionTemplateFromProvider);
    }

    private void recreateAssociatedAssetClasses(
        Provider provider,
        String missionTplId,
        MissionTemplate missionTemplate,
        MissionTemplateResponse.MissionTemplate missionTemplateFromProvider) {
      // Note: complete recreation of the asset classes association does not have any cascading
      // effect on missions
      // (as it would be the case for parameters if completely recreated), but any possible removal
      // of an asset
      // class in the template would render associated missions invalid in terms of the associated
      // asset.
      // TODO(low prio) perhaps some more sophisticated handling/error-reporting, etc.
      var acsDeleted =
          missionTemplateAssetClassService.deleteForMissionTemplate(
              provider.providerId, missionTplId);
      log.debug("preUpdateMissionTpl: acsDeleted=>{}", acsDeleted);
      if (missionTemplateFromProvider.assetClassNames != null) {
        createAssociatedAssetClasses(
            provider, missionTemplate, missionTemplateFromProvider.assetClassNames);
      }
    }

    private void refreshAssociatedParameters(
        Provider provider,
        String missionTplId,
        MissionTemplateResponse.MissionTemplate missionTemplateFromProvider) {
      log.debug(
          "missionTemplateFromProvider.parameters=>{}", missionTemplateFromProvider.parameters);

      if (missionTemplateFromProvider.parameters.isEmpty()) {
        // just remove any parameters that may have been previously captured:
        var psDeleted =
            parameterService.deleteForMissionTemplate(provider.providerId, missionTplId);
        log.debug("preUpdateMissionTpl: psDeleted=>{}", psDeleted);
        return;
      }

      //  - delete parameters no longer reported by provider for this template
      //  - refresh parameters that are already used by missions
      //  - add any new parameters reported by provider

      Map<String, MissionTemplateResponse.Parameter> byParamNameFromProvider = new HashMap<>();
      missionTemplateFromProvider.parameters.forEach(
          param -> byParamNameFromProvider.put(param.paramName, param));

      final var paramNamesFromProvider =
          missionTemplateFromProvider.parameters.stream()
              .map(p -> p.paramName)
              .collect(Collectors.toSet());

      var currentParameters = parameterService.getParameters(provider.providerId, missionTplId);
      var currentParamNames = currentParameters.stream().map(p -> p.paramName).toList();

      var withReferringArguments =
          argumentService.getArgumentsWithParameterNames(
              provider.providerId, missionTplId, currentParamNames);

      var paramNamesToUpdate = new HashSet<String>();
      for (Argument referringArg : withReferringArguments) {
        if (paramNamesFromProvider.contains(referringArg.paramName)) {
          paramNamesToUpdate.add(referringArg.paramName);
        }
      }
      var paramNamesToAdd = new HashSet<>(paramNamesFromProvider);
      paramNamesToAdd.removeAll(paramNamesToUpdate);

      log.debug("paramNamesToUpdate={} paramNamesToAdd={}", paramNamesToUpdate, paramNamesToAdd);

      // delete all params except the ones to be updated:
      var psDeleted =
          parameterService.deleteForMissionTemplateExcept(
              provider.providerId, missionTplId, paramNamesToUpdate.stream().toList());
      log.debug("preUpdateMissionTpl: psDeleted=>{}", psDeleted);

      // do the updates
      paramNamesToUpdate.forEach(
          paramName -> {
            var param =
                createParameterFromProvider(
                    provider, missionTplId, byParamNameFromProvider.get(paramName));
            parameterService.updateParameter(param);
          });

      // (re)create the rest:
      paramNamesToAdd.forEach(
          paramName -> {
            var param =
                createParameterFromProvider(
                    provider, missionTplId, byParamNameFromProvider.get(paramName));
            parameterService.createParameter(param);
          });
    }

    private Parameter createParameterFromProvider(
        Provider provider,
        String missionTplId,
        MissionTemplateResponse.Parameter paramFromProvider) {
      var param = new Parameter(provider.providerId, missionTplId, paramFromProvider.paramName);
      param.type = paramFromProvider.type;
      param.required = paramFromProvider.required;
      param.defaultValue = paramFromProvider.defaultValue;
      param.defaultUnits = paramFromProvider.defaultUnits;
      param.valueCanReference = paramFromProvider.valueCanReference;
      param.description = paramFromProvider.description;
      return param;
    }

    public void preUpdateMission(Provider provider, Mission pl) {
      log.debug("preUpdateMission: pl={}", pl);

      // get the current state of the mission:
      var mission = missionService.getMission(pl.providerId, pl.missionTplId, pl.missionId);
      log.debug("preUpdateMission: saved mission={}", mission);

      // depending on current missionStatus:
      if (mission.missionStatus == MissionStatusType.DRAFT) {
        // is mission being submitted?
        if (pl.missionStatus == MissionStatusType.SUBMITTED) {
          log.debug("preUpdateMission: submitting, pl={}", Utl.writeJson(pl));
          submitMission(mission, pl);
        } else if (pl.missionStatus == null || pl.missionStatus == MissionStatusType.DRAFT) {
          // OK, no requested change in status; let mutation proceed.
          pl.setUpdatedDate(OffsetDateTime.now());
        } else {
          // from DRAFT, only SUBMITTED is allowed.
          throw new Error(
              String.format("Unexpected pl.missionStatus=%s in DRAFT status", pl.missionStatus));
        }
      } else if (pl.noPatch()) {
        // This is a request for refreshing the mission status.
        if (provider.canReportMissionStatus) {
          retrieveMissionStatus(mission.providerMissionId, pl);
        } else {
          log.warn("provider '{}' does not support reporting mission status", provider.providerId);
        }
      } else {
        throw new IllegalStateException("Unexpected pl.missionStatus: " + pl.missionStatus);
      }
    }

    private void submitMission(Mission mission, Mission pl) {
      var args =
          argumentService.getArguments(mission.providerId, mission.missionTplId, mission.missionId);

      PostMissionPayload pmpl = new PostMissionPayload();

      pmpl.arguments = new HashMap<>();
      args.forEach(
          a ->
              pmpl.arguments.put(
                  a.paramName,
                  new PostMissionPayload.MissionArgValueAndUnits(a.paramValue, a.paramUnits)));

      pmpl.missionTplId = mission.missionTplId;
      pmpl.assetId = mission.assetId;
      pmpl.description = mission.description;
      pmpl.schedType = mission.schedType == null ? null : mission.schedType.name();
      pmpl.schedDate = mission.schedDate == null ? null : mission.schedDate.toString();

      log.debug("submitMission: pmpl={}", Utl.writeJson(pmpl));
      var res = mxmProviderClient.postMission(pmpl);
      log.debug("submitMission: res={}", Utl.writeJson(res));
      if (MissionStatusType.SUBMITTED.name().equals(res.result.status)) {
        pl.missionStatus = MissionStatusType.SUBMITTED;
        pl.providerMissionId = res.result.missionId;
      } else {
        log.warn("unexpected mission status: {}", res.result.status);
      }
    }

    private void retrieveMissionStatus(String providerMissionId, Mission pl) {
      if (providerMissionId == null) {
        log.warn("retrieveMissionStatus: providerMissionId is null");
        return;
      }
      log.debug("retrieveMissionStatus: providerMissionId='{}'", providerMissionId);

      try {
        var ms = mxmProviderClient.getMissionStatus(providerMissionId);
        if (ms.result.status == null) {
          // no change in status (TODO this special null meaning is temporary)
          log.warn("retrieveMissionStatus: provider reported a null mission status");
          return;
        }
        pl.missionStatus = MissionStatusType.valueOf(ms.result.status);
        pl.setUpdatedDate(OffsetDateTime.now());
      } catch (Exception e) {
        log.warn("retrieveMissionStatus: exception: {}", e.getMessage());
      }
    }

    public void done() {
      mxmProviderClient.done();
    }
  }
}
