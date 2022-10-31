package org.mbari.mxm.graphql;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.graphql.*;
import org.mbari.mxm.Broadcaster;
import org.mbari.mxm.ProviderManager;
import org.mbari.mxm.Utl;
import org.mbari.mxm.db.argument.Argument;
import org.mbari.mxm.db.argument.ArgumentService;
import org.mbari.mxm.db.asset.Asset;
import org.mbari.mxm.db.asset.AssetService;
import org.mbari.mxm.db.assetClass.AssetClass;
import org.mbari.mxm.db.assetClass.AssetClassService;
import org.mbari.mxm.db.mission.Mission;
import org.mbari.mxm.db.mission.MissionService;
import org.mbari.mxm.db.missionTemplate.MissionTemplate;
import org.mbari.mxm.db.missionTemplate.MissionTemplateService;
import org.mbari.mxm.db.missionTemplateAssetClass.MissionTemplateAssetClass;
import org.mbari.mxm.db.missionTemplateAssetClass.MissionTemplateAssetClassService;
import org.mbari.mxm.db.parameter.Parameter;
import org.mbari.mxm.db.parameter.ParameterService;
import org.mbari.mxm.db.provider.Provider;
import org.mbari.mxm.db.provider.ProviderService;
import org.mbari.mxm.db.unit.Unit;
import org.mbari.mxm.db.unit.UnitService;
import org.mbari.mxm.graphql.exc.DbViolationException;
import org.mbari.mxm.graphql.exc.MxmException;
import org.mbari.mxm.graphql.exc.ProviderFailureException;
import org.mbari.mxm.provider_client.responses.MissionValidationResponse;
import org.mbari.mxm.provider_client.responses.PingResponse;

@GraphQLApi
@ApplicationScoped
@Slf4j
public class MxmGraphQLEndpoint {

  @Inject ProviderService providerService;

  @Inject AssetClassService assetClassService;

  @Inject AssetService assetService;

  @Inject MissionTemplateService missionTemplateService;

  @Inject MissionTemplateAssetClassService missionTemplateAssetClassService;

  @Inject UnitService unitService;

  @Inject ParameterService parameterService;

  @Inject MissionService missionService;

  @Inject ArgumentService argumentService;

  @Inject ProviderManager providerManager;

  private ProviderManager.PMInstance createProviderManager(Provider pl) {
    return providerManager.createInstance(
        pl.getProviderId(), pl.getHttpEndpoint(), pl.getApiType());
  }

  ///////////////////////////////////////////////////////////////////
  // providers

  @Query
  @Description("Get all providers")
  public List<Provider> allProviders() {
    return providerService.getProviders();
  }

  @Query
  @Description("Get a provider")
  public Provider provider(@Name("providerId") String providerId) {
    return providerService.getProvider(providerId);
  }

  @Mutation
  @Description("Ping a provider endpoint")
  public PingResponse pingProvider(@Valid Provider pl) throws ProviderPingException {
    var pm = createProviderManager(pl);
    PingResponse pr;
    try {
      pr = pm.ping();
    } catch (Exception e) {
      throw new ProviderFailureException(
          String.format(
              """
          Failure while trying to ping provider.

          The provider service may be down (%s).
          If the problem persists, please contact the provider administrator.
          """,
              pl.httpEndpoint),
          e);
    }
    if (pr.result.datetime == null) {
      throw new ProviderFailureException(
          String.format(
              """
          Provider did not return expected ping response.

          Provider service: %s.
          """,
              pl.httpEndpoint));
    }
    return pr;
  }

  @Mutation
  @Description("Register a new provider")
  public Provider createProvider(@Valid ProviderCreate pl) {
    var provider = new Provider(pl.providerId);
    provider.setHttpEndpoint(pl.httpEndpoint);
    provider.setApiType(pl.apiType);
    var pm = createProviderManager(provider);
    pm.preInsertProvider(provider);
    try {
      var created = providerService.createProvider(provider);
      pm.postInsertProvider(created);
      pm.done();
      return created;
    } catch (Exception e) {
      throw handleException(pl.httpEndpoint, "registering provider", e);
    }
  }

  @Subscription
  @Description("Get notified when a provider registration has completed")
  public Multi<Provider> providerCreated() {
    return providerService.getBroadcaster().createProcessor(Broadcaster.EventType.CREATED);
  }

  @Mutation
  @Description("Update a provider")
  public Provider updateProvider(@Valid Provider pl) {
    log.debug("updateProvider: pl={}", pl);
    assert pl.providerId != null;
    return providerService.updateProvider(pl);
  }

  @Mutation
  @Description("Delete a provider")
  public Provider deleteProvider(Provider pl) {
    log.debug("deleteProvider: pl={}", pl);
    return providerService.deleteProvider(pl);
  }

  @Subscription
  @Description("Get notified when a provider is updated")
  public Multi<Provider> providerUpdated() {
    return providerService.getBroadcaster().createProcessor(Broadcaster.EventType.UPDATED);
  }

  @Subscription
  @Description("Get notified while interactions with a provider are performed")
  public Multi<ProviderProgress> providerProgress(@Name("providerId") String providerId) {
    log.debug("providerProgress: providerId='{}'", providerId);
    return providerService.getProgressBroadcaster().createProcessor(Utl.primaryKey(providerId));
  }

  @Subscription
  @Description("Get notified when a specific provider is updated")
  public Multi<Provider> providerUpdatedById(@Name("providerId") String providerId) {
    return providerService
        .getBroadcaster()
        .createProcessor(Broadcaster.EventType.UPDATED, Utl.primaryKey(providerId));
  }

  @Subscription
  @Description("Get notified when a provider is deleted")
  public Multi<Provider> providerDeleted() {
    return providerService.getBroadcaster().createProcessor(Broadcaster.EventType.DELETED);
  }

  ///////////////////////////////////////////////////////////////////
  // assetClasses

  @Query
  @Description("Get all asset classes")
  public List<AssetClass> allAssetClasses() {
    return assetClassService.getAllAssetClasses();
  }

  @Query
  @Description("Get an asset class")
  public AssetClass assetClass(@Name("className") String className) {
    return assetClassService.getAssetClass(className);
  }

  @Query
  @Description("Get the asset classes used by a provider")
  public List<AssetClass> assetClassesForProvider(@Name("providerId") String providerId) {
    var list =
        missionTemplateAssetClassService.getAssetClassesMultipleProviders(
            List.of(String.format("'%s'", providerId)));
    return list.isEmpty() ? List.of() : list.get(0);
  }

  ///////////////////////////////////////////////////////////////////
  // assets

  @Query
  @Description("Get all assets")
  public List<Asset> allAssets() {
    return assetService.getAllAssets();
  }

  @Query
  @Description("Get an asset")
  public Asset asset(@Name("assetId") String assetId) {
    return assetService.getAsset(assetId);
  }

  ///////////////////////////////////////////////////////////////////
  // MissionTemplates

  @Query
  @Description("Get all mission templates")
  public List<MissionTemplate> allMissionTemplates() {
    return missionTemplateService.getAllMissionTemplates();
  }

  @Query
  @Description("Get a mission template")
  public MissionTemplate missionTemplate(
      @Name("providerId") String providerId, @Name("missionTplId") String missionTplId) {
    return missionTemplateService.getMissionTemplate(providerId, missionTplId);
  }

  @Query
  @Description("Get mission templates of a provider")
  public List<MissionTemplate> missionTemplatesForProvider(@Name("providerId") String providerId) {
    return missionTemplateService.getMissionTemplates(providerId);
  }

  @Query
  @Description("Gets the mission templates under the given directory.")
  public List<MissionTemplate> listMissionTplsDirectory(
      @Name("providerId") String providerId, @Name("directory") String directory) {
    return missionTemplateService.listMissionTplsDirectory(providerId, directory);
  }

  @Mutation
  @Description("Update a mission template, done against the provider")
  public MissionTemplate updateMissionTemplate(@Valid MissionTemplate pl) {
    log.debug("updateMissionTemplate: pl={}", pl);
    var provider = providerService.getProvider(pl.getProviderId());
    var pm = createProviderManager(provider);
    try {
      pm.preUpdateMissionTemplate(provider, pl);
      var res = missionTemplateService.updateMissionTemplate(pl);
      pm.done();
      return res;
    } catch (Exception e) {
      throw handleException(provider.httpEndpoint, "refreshing mission template from provider", e);
    }
  }

  ///////////////////////////////////////////////////////////////////
  // MissionTemplateAssetClass

  @Query
  @Description("Get all mission template asset classes")
  public List<MissionTemplateAssetClass> allMissionTemplateAssetClasses() {
    return missionTemplateAssetClassService.getAllMissionTemplateAssetClasses();
  }

  @Query
  @Description("Get mission template asset classes of a provider")
  public List<MissionTemplateAssetClass> missionTemplateAssetClasses(String providerId) {
    return missionTemplateAssetClassService.getMissionTemplateAssetClasses(providerId);
  }

  @Query
  @Description("Get asset class names of a mission template")
  public List<String> assetClassNames(String providerId, String missionTplId) {
    return missionTemplateAssetClassService.getAssetClassNames(providerId, missionTplId);
  }

  ///////////////////////////////////////////////////////////////////
  // Units

  @Query
  @Description("Get all units")
  public List<Unit> allUnits() {
    return unitService.getAllUnits();
  }

  @Query
  @Description("Get a unit")
  public Unit unit(@Name("unitName") String unitName) {
    return unitService.getUnit(unitName);
  }

  ///////////////////////////////////////////////////////////////////
  // Parameters

  @Query
  @Description("Get all parameters")
  public List<Parameter> allParameters() {
    return parameterService.getAllParameters();
  }

  @Query
  @Description("Get all parameters of a mission template")
  public List<Parameter> parameters(
      @Name("providerId") String providerId, @Name("missionTplId") String missionTplId) {
    return parameterService.getParameters(providerId, missionTplId);
  }

  @Query
  @Description("Get a parameter")
  public Parameter parameter(
      @Name("providerId") String providerId,
      @Name("missionTplId") String missionTplId,
      @Name("paramName") String paramName) {
    return parameterService.getParameter(providerId, missionTplId, paramName);
  }

  ///////////////////////////////////////////////////////////////////
  // Missions

  @Query
  @Description("Get all missions")
  public List<Mission> allMissions() {
    return missionService.getAllMissions();
  }

  @Query
  @Description("Get all missions of a provider")
  public List<Mission> missionsForProvider(@Name("providerId") String providerId) {
    return missionService.getMissionsForProvider(providerId);
  }

  @Query
  @Description("Get all missions of a mission template")
  public List<Mission> missionsForTemplate(
      @Name("providerId") String providerId, @Name("missionTplId") String missionTplId) {
    return missionService.getMissionsForTemplate(providerId, missionTplId);
  }

  @Query
  @Description("Get a mission")
  public Mission mission(
      @Name("providerId") String providerId,
      @Name("missionTplId") String missionTplId,
      @Name("missionId") String missionId) {
    return missionService.getMission(providerId, missionTplId, missionId);
  }

  @Mutation
  @Description("Create a new mission")
  public Mission createMission(@Valid Mission pl) {
    return missionService.createMission(pl);
  }

  @Mutation
  @Description("Update a mission")
  public Mission updateMission(@Valid Mission pl) {
    log.debug("updateMission: pl={}", pl);
    var provider = providerService.getProvider(pl.getProviderId());
    var pm = createProviderManager(provider);

    try {
      pm.preUpdateMission(provider, pl);
      var res = missionService.updateMission(pl);

      // not critical but attempt to broadcast mission status update here
      // (in particular, upon just submitting the mission)
      // was triggering subscription related issues.

      pm.done();
      return res;
    } catch (Exception e) {
      throw handleException(provider.httpEndpoint, "refreshing mission from provider", e);
    }
  }

  @Mutation
  @Description("Validate a mission against the provider")
  public MissionValidationResponse validateMission(@Valid Mission pl) {
    log.debug("validateMission: pl={}", pl);
    var provider = providerService.getProvider(pl.getProviderId());
    var pm = createProviderManager(provider);
    try {
      var res = pm.validateMission(pl);
      pm.done();
      return res;
    } catch (Exception e) {
      throw handleException(provider.httpEndpoint, "validating mission against provider", e);
    }
  }

  @Mutation
  @Description("Delete a mission")
  public Mission deleteMission(Mission pl) {
    return missionService.deleteMission(pl);
  }

  @Subscription
  @Description("Get notified when a mission is created")
  public Multi<Mission> missionCreated() {
    return missionService.getBroadcaster().createProcessor(Broadcaster.EventType.CREATED);
  }

  @Subscription
  @Description("""
    Get notified when a mission or any argument is updated.
    """)
  public Multi<Mission> missionUpdated() {
    return missionService.getBroadcaster().createProcessor(Broadcaster.EventType.UPDATED);
  }

  @Subscription
  @Description(
      """
    Get notified when a particular mission or any of its arguments is updated.
    """)
  public Multi<Mission> missionUpdatedById(
      @Name("providerId") String providerId,
      @Name("missionTplId") String missionTplId,
      @Name("missionId") String missionId) {
    return missionService
        .getBroadcaster()
        .createProcessor(
            Broadcaster.EventType.UPDATED, Utl.primaryKey(providerId, missionTplId, missionId));
  }

  @Subscription
  @Description("Get notified when a mission is deleted")
  public Multi<Mission> missionDeleted() {
    return missionService.getBroadcaster().createProcessor(Broadcaster.EventType.DELETED);
  }

  ///////////////////////////////////////////////////////////////////
  // Arguments

  // Some individual queries for arguments, but typically this information is
  // to be obtained via mission.

  @Query
  @Description("Get the arguments defined in a mission")
  public List<Argument> getArguments(
      @Name("providerId") String providerId,
      @Name("missionTplId") String missionTplId,
      @Name("missionId") String missionId) {
    return argumentService.getArguments(providerId, missionTplId, missionId);
  }

  @Query
  @Description("Get an argument")
  public Argument getArgument(
      @Name("providerId") String providerId,
      @Name("missionTplId") String missionTplId,
      @Name("missionId") String missionId,
      @Name("paramName") String paramName) {
    return argumentService.getArgument(providerId, missionTplId, missionId, paramName);
  }

  @Mutation
  @Description("Create a new argument")
  public Argument createArgument(@Valid Argument pl) {
    return argumentService.createArgument(pl);
  }

  @Mutation
  @Description("Update an argument")
  public Argument updateArgument(@Valid Argument pl) {
    return argumentService.updateArgument(pl);
  }

  @Mutation
  @Description("Delete a argument")
  public Argument deleteArgument(Argument pl) {
    return argumentService.deleteArgument(pl);
  }

  ///////////////////////////////////////////////////////////////

  // Already functional but somewhat ad hoc
  private MxmException handleException(String httpEndpoint, String activity, Exception e) {
    Throwable thr = e;
    while (thr != null) {
      if (thr instanceof org.postgresql.util.PSQLException) {
        // likely a foreign key constraint violation
        throw new DbViolationException(
            String.format(
                """
                Failure while %s.

                A database violation may have occurred caused by a reference to an invalid/missing
                entity by this provider. Please report this issue.

                Exception: %s
                """,
                activity, thr.getMessage()),
            e);
      }
      thr = thr.getCause();
    }

    return new ProviderFailureException(
        String.format(
            """
          Failure while trying to ping provider.

          The provider service may be down (%s).
          If the problem persists, please contact the provider administrator.
          """,
            httpEndpoint),
        e);
  }
}
