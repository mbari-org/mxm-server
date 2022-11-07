package org.mbari.mxm;

import java.time.OffsetDateTime;
import java.util.*;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.mbari.mxm.db.argument.Argument;
import org.mbari.mxm.db.argument.ArgumentService;
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
import org.mbari.mxm.db.provider.ProviderService;
import org.mbari.mxm.graphql.ProviderPingException;
import org.mbari.mxm.graphql.ProviderProgress;
import org.mbari.mxm.provider_client.MxmProviderClient;
import org.mbari.mxm.provider_client.MxmProviderClientBuilder;
import org.mbari.mxm.provider_client.responses.MissionTemplateResponse;
import org.mbari.mxm.provider_client.responses.MissionValidationResponse;
import org.mbari.mxm.provider_client.responses.PingResponse;
import org.mbari.mxm.provider_client.rest.MxmInfo;
import org.mbari.mxm.provider_client.rest.PostMissionRequest;
import org.mbari.mxm.rest.MissionStatus;

@ApplicationScoped
@Slf4j
public class ProviderManager {

  @Inject ProviderService providerService;

  @Inject MissionTemplateService missionTemplateService;

  @Inject MissionTemplateAssetClassService missionTemplateAssetClassService;

  @Inject ParameterService parameterService;

  @Inject MissionService missionService;

  @Inject ArgumentService argumentService;

  // TODO more standard way to handle mxm.external.url/MXM_EXTERNAL_URL
  private static final String MXM_EXTERNAL_URL = System.getenv("MXM_EXTERNAL_URL");

  @ConfigProperty(name = "mxm.external.url")
  String mxmExternalUrl;

  public PMInstance createInstance(
      String providerId, String httpEndpoint, ProviderApiType apiType) {

    return new PMInstance(providerId, httpEndpoint, apiType);
  }

  public class PMInstance {

    private final MxmProviderClient mxmProviderClient;
    private final ProviderProgress providerProgress;

    private final MxmInfo mxmInfo;

    PMInstance(String providerId, String httpEndpoint, ProviderApiType apiType) {
      log.debug(
          "PMInstance: providerId={}, httpEndpoint={}, apiType={}",
          providerId,
          httpEndpoint,
          apiType);
      mxmProviderClient = MxmProviderClientBuilder.create(providerId, httpEndpoint, apiType);

      providerProgress = ProviderProgress.builder().providerId(providerId).build();

      // TODO factor handling of this with wrt SPARouting
      final var serverLoc = Objects.requireNonNullElseGet(MXM_EXTERNAL_URL, () -> mxmExternalUrl);
      mxmInfo = new MxmInfo();
      mxmInfo.mxmRestEndpoint = serverLoc;
    }

    private void broadcastProgress() {
      providerService.getProgressBroadcaster().broadcast(providerProgress);
    }

    private void broadcastProgress(String message) {
      providerProgress.message = message;
      broadcastProgress();
    }

    private void broadcastProgress(String message, Double percentComplete) {
      providerProgress.message = message;
      providerProgress.percentComplete = percentComplete;
      broadcastProgress();
    }

    public PingResponse ping() throws ProviderPingException {
      return mxmProviderClient.ping(mxmInfo);
    }

    public void preInsertProvider(Provider provider) {
      log.debug("preInsertProvider: provider={}", provider);

      // TODO this try/catch is mainly for the TFT@TSAUV provider, which doesn't support `ping` yet
      try {
        var pong = mxmProviderClient.ping(mxmInfo);
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
      broadcastProgress("Provider entry created", null);
      getAndCreateMissionTemplatesFromRoot(provider);
    }

    private void getAndCreateMissionTemplatesFromRoot(Provider provider) {
      // create a MissionTemplate entry for the directory itself:
      getAndCreateMissionTemplate(provider, "/");

      // get all directory entries, recursively as specified in MXM Provider API:
      var missionTplListing = mxmProviderClient.getMissionTemplates("");
      if (missionTplListing.result.entries != null) {
        createMissionTemplatesForDirectoryEntries(provider, missionTplListing.result.entries);
      } else {
        log.warn("getAndCreateMissionTemplatesFromRoot: no entries");
      }
    }

    private void createMissionTemplate(MissionTemplate missionTemplate) {
      missionTemplateService.createMissionTemplate(missionTemplate);
      broadcastProgress(missionTemplate.missionTplId);
    }

    private void updateMissionTemplate(MissionTemplate missionTemplate) {
      missionTemplateService.updateMissionTemplate(missionTemplate);
      broadcastProgress(missionTemplate.missionTplId);
    }

    private void createMissionTemplatesForDirectoryEntries(
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
            createMissionTemplate(missionTemplate);

            if (isDirectory) {
              if (entry.entries != null && entry.entries.size() > 0) {
                createMissionTemplatesForDirectoryEntries(provider, entry.entries);
              }
            } else {
              // just add the associated asset classes to the mission template:
              if (entry.assetClassNames != null) {
                createAssociatedAssetClasses(provider, missionTemplate, entry.assetClassNames);
              }
            }
          });
    }

    private void getAndCreateMissionTemplate(Provider provider, String missionTplId) {
      missionTplId = Utl.cleanPath(missionTplId);
      final var isDirectory = missionTplId.endsWith("/");

      MissionTemplate missionTemplate = new MissionTemplate(provider.providerId, missionTplId);
      missionTemplate.retrievedAt = OffsetDateTime.now();

      if (isDirectory) {
        // no info needed from provider, just create the entry:
        createMissionTemplate(missionTemplate);
      } else {
        // actual template, get info from provider as needed:
        log.debug("getAndCreateMissionTemplate: missionTplId='{}'", missionTplId);
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

      createMissionTemplate(missionTemplate);

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

    /** Refreshes mission template information from the provider. */
    public void preUpdateMissionTemplate(Provider provider, MissionTemplate pl) {
      log.debug("preUpdateMissionTemplate: missionTemplate={}", pl);
      broadcastProgress("Updating " + pl.missionTplId, null);
      final var missionTplId = pl.missionTplId;
      if (missionTplId.endsWith("/")) {
        updateMissionTemplateDirectory(provider, missionTplId);
      } else {
        updateActualMissionTemplate(provider, pl);
      }
    }

    private void updateActualMissionTemplate(Provider provider, MissionTemplate pl) {
      log.debug("refreshing template missionTplId='{}'", pl.missionTplId);
      var response = mxmProviderClient.getMissionTemplate(pl.missionTplId);
      var missionTemplateFromProvider = response.result;

      // update mission template itself:
      pl.description = missionTemplateFromProvider.description;
      pl.retrievedAt = OffsetDateTime.now();
      updateMissionTemplate(pl);

      recreateAssociatedAssetClasses(provider, pl.missionTplId, pl, missionTemplateFromProvider);

      refreshAssociatedParameters(provider, pl.missionTplId, missionTemplateFromProvider);
    }

    private void updateMissionTemplateDirectory(Provider provider, String missionTplId) {
      log.debug("updateMissionTemplateDirectory: missionTplId='{}'", missionTplId);
      var response = mxmProviderClient.getMissionTemplates(missionTplId);
      var missionTemplateList = response.result;
      if (missionTemplateList.entries.isEmpty()) {
        deleteAllMissionTemplatesUnderDirectory(provider, missionTplId);
      } else {
        updateMissionTemplateDirectoryItself(provider, missionTplId);
        updateMissionTemplateDirectoryEntries(provider, missionTemplateList.entries);
      }
    }

    private void deleteAllMissionTemplatesUnderDirectory(Provider provider, String missionTplId) {
      var res = missionTemplateService.deleteDirectoryRecursive(provider.providerId, missionTplId);
      log.debug("preUpdateMissionTemplate: no templates under {}, res=>{}", missionTplId, res);
    }

    private void updateMissionTemplateDirectoryItself(Provider provider, String missionTplId) {
      // just the retrievedAt timestamp:
      updateMissionTemplate(
          new MissionTemplate(provider.providerId, missionTplId, null, OffsetDateTime.now()));
    }

    private void updateMissionTemplateDirectoryEntries(
        Provider provider, List<MissionTemplateResponse.MissionTemplate> entries) {
      entries.forEach(
          entry -> {
            final var missionTplId = Utl.cleanPath(entry.missionTplId);
            final var isDirectory = missionTplId.endsWith("/");
            if (isDirectory) {
              if (entry.entries == null || entry.entries.isEmpty()) {
                deleteAllMissionTemplatesUnderDirectory(provider, missionTplId);
              } else {
                updateMissionTemplateDirectoryItself(provider, missionTplId);
                updateMissionTemplateDirectoryEntries(provider, entry.entries);
              }
            }
            broadcastProgress(missionTplId);
          });
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
      log.trace("preUpdateMissionTemplate: acsDeleted=>{}", acsDeleted);
      if (missionTemplateFromProvider.assetClassNames != null) {
        createAssociatedAssetClasses(
            provider, missionTemplate, missionTemplateFromProvider.assetClassNames);
      }
    }

    // captures parameter from provider including its order
    @AllArgsConstructor
    private static class ParameterWithOrder {
      MissionTemplateResponse.Parameter parameter;
      int paramOrder;
    }

    private void refreshAssociatedParameters(
        Provider provider,
        String missionTplId,
        MissionTemplateResponse.MissionTemplate missionTemplateFromProvider) {
      log.trace(
          "missionTemplateFromProvider.parameters={}",
          missionTemplateFromProvider.parameters.size());

      if (missionTemplateFromProvider.parameters.isEmpty()) {
        // just remove any parameters that may have been previously captured:
        var psDeleted =
            parameterService.deleteForMissionTemplate(provider.providerId, missionTplId);
        log.debug(
            "refreshAssociatedParameters: preUpdateMissionTemplate: psDeleted=>{}", psDeleted);
        return;
      }

      //  - delete parameters no longer reported by provider for this template
      //  - refresh parameters that are already used by missions
      //  - add any new parameters reported by provider

      // indexed by name, captures parameters from provider, including paramOrder:
      HashMap<String, ParameterWithOrder> byParamNameFromProvider =
          missionTemplateFromProvider.parameters.stream()
              .collect(
                  HashMap::new,
                  (map, p) -> map.put(p.paramName, new ParameterWithOrder(p, map.size())),
                  (map, map2) -> {});

      final var paramNamesFromProvider =
          missionTemplateFromProvider.parameters.stream().map(p -> p.paramName).toList();
      log.trace("paramNamesFromProvider={}", paramNamesFromProvider);

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

      log.trace("paramNamesToUpdate={} paramNamesToAdd={}", paramNamesToUpdate, paramNamesToAdd);

      // delete all params except the ones to be updated:
      var psDeleted =
          parameterService.deleteForMissionTemplateExcept(
              provider.providerId, missionTplId, paramNamesToUpdate.stream().toList());
      log.trace("preUpdateMissionTemplate: psDeleted=>{}", psDeleted);

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
        Provider provider, String missionTplId, ParameterWithOrder paramWithOrder) {
      log.trace(
          "paramWithOrder: name={} order={}",
          paramWithOrder.parameter.paramName,
          paramWithOrder.paramOrder);
      MissionTemplateResponse.Parameter paramFromProvider = paramWithOrder.parameter;
      var param = new Parameter(provider.providerId, missionTplId, paramFromProvider.paramName);
      param.paramOrder = paramWithOrder.paramOrder;
      param.type = paramFromProvider.type;
      param.required = paramFromProvider.required;
      param.defaultValue = paramFromProvider.defaultValue;
      param.defaultUnits = paramFromProvider.defaultUnits;
      param.valueCanReference = paramFromProvider.valueCanReference;
      param.description = paramFromProvider.description;
      return param;
    }

    public List<MissionStatus.StatusUpdate> preUpdateMission(Provider provider, Mission pl) {
      log.debug("preUpdateMission: pl={}", pl);

      // get the current state of the mission:
      var mission = missionService.getMission(pl.providerId, pl.missionTplId, pl.missionId);
      log.debug("preUpdateMission: saved mission={}", mission);

      // depending on current missionStatus:
      if (mission.missionStatus == MissionStatusType.DRAFT) {
        // is mission being submitted?
        if (pl.missionStatus == MissionStatusType.SUBMITTED) {
          log.debug("preUpdateMission: submitting, pl={}", Utl.writeJson(pl));
          return submitMission(mission, pl);
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
      return null;
    }

    public MissionValidationResponse validateMission(Mission mission) {
      PostMissionRequest pmpl = preparePostMissionPayload(mission);
      log.debug("validateMission: pmpl={}", Utl.writeJson(pmpl));
      var res = mxmProviderClient.validateMission(pmpl);
      log.debug("validateMission: res={}", Utl.writeJson(res));
      return res;
    }

    private List<MissionStatus.StatusUpdate> submitMission(Mission mission, Mission pl) {
      PostMissionRequest pmpl = preparePostMissionPayload(mission);

      log.debug("submitMission: pmpl={}", Utl.writeJson(pmpl));
      var res = mxmProviderClient.postMission(pmpl);
      log.debug("submitMission: res={}", Utl.writeJson(res));

      final var status = res.result().getStatus();
      if (MissionStatusType.SUBMITTED == status) {
        pl.missionStatus = MissionStatusType.SUBMITTED;
        pl.providerMissionId = res.result().providerMissionId;
        return res.result().statusUpdates;
      } else {
        log.warn("unexpected mission status: {}", status);
        return null;
      }
    }

    private PostMissionRequest preparePostMissionPayload(Mission mission) {
      var args =
          argumentService.getArguments(mission.providerId, mission.missionTplId, mission.missionId);

      PostMissionRequest pmpl = new PostMissionRequest();
      args.forEach(
          a ->
              pmpl.arguments.add(
                  new PostMissionRequest.MissionArgValueAndUnits(
                      a.paramName, a.paramValue, a.paramUnits)));

      pmpl.providerId = mission.providerId;
      pmpl.missionTplId = mission.missionTplId;
      pmpl.missionId = mission.missionId;
      pmpl.assetId = mission.assetId;
      pmpl.description = mission.description;
      pmpl.schedType = mission.schedType == null ? null : mission.schedType.name();
      pmpl.schedDate = mission.schedDate == null ? null : mission.schedDate.toString();
      return pmpl;
    }

    private void retrieveMissionStatus(String providerMissionId, Mission pl) {
      if (providerMissionId == null) {
        log.warn("retrieveMissionStatus: providerMissionId is null");
        return;
      }
      log.debug("retrieveMissionStatus: providerMissionId='{}'", providerMissionId);

      try {
        var ms = mxmProviderClient.getMissionStatus(providerMissionId);
        if (ms.result().status == null) {
          // no change in status (TODO this special null meaning is temporary)
          log.warn("retrieveMissionStatus: provider reported a null mission status");
          return;
        }
        pl.missionStatus = MissionStatusType.valueOf(ms.result().status);
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
