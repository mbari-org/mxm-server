2022-10

- add `missionTemplates` and `providers` member resolution for `AssetClass` (graphql)
- data sql scripts to initialize the database
  - `just update-sql-init-scripts` regenerates the ones that use entities from TethysDash:
    - Units of measure
    - Set of LRAUV assets
  - Other entities (Waveglider- and "Acme"- related) are manually entered in the scripts.
  - The same set of init scripts is also used for the tests.

- Model revision: 
  - `Unit` model now by itself
    - So, the set of units of measure is to be a common resource
    - Initially to be populated from the LRAUV/TethysDash system
  - `AssetClass` and `Asset` models now by themselves
    - As before, provider continues to indicate the `AssetClass`es used by its mission templates.
    - This is in preparation for eventual integration of the TrackingDB information *or* a similar common
      place that defines the asset classes (types) and assets (platforms) referenceable by any provider.
    - for convenience, adding some asset classes and assets at database setup time.

2022-09

- adding mission validation
  - TODO better response model at graphql level
- fix getAssetClassesMultiple
- renamed ProviderApiType enum value to `REST` (from `REST0`)
- fix `assets` member resolution for `AssetClass` (graphql)
- adjust PostMissionPayload to use a List for the arguments
- basic branding (only in swagger-ui)
- similarly as with `MXM_EXTERNAL_URL`, new env var `MXM_EXTERNAL_WS_URL` that 
  can be used to specify the external URL for the websocket endpoint.
- websocket connection starting to work when deploying server on the mxm machine.
  (BTW, `websocat` seems to expose a Quarkus issue: quarkusio/quarkus/issues/28190)
- set version 0.9.2 toward release with overhauled version of the UI
- set `/mxmConfig.json` as the path for the UI to retrieve the configuration.
- added MxmConfig for the UI
- added parameter field to Argument (graphql)
- added some provider summary fields (numActualMissionTemplates, numMissions, numAssets, etc)

2022-08

- version 0.9.1
- capture parameter order as reported from provider
- mission template update adjustments
- improved handling of updating missions wrt to templates
- added providerMissionId to mission model: captures id given by provider when submitting the mission.
  Such ID used when requesting status of a mission.
  TODO set missionId as generated as identity (not prompted to user)
- better mission template handling by expecting recursive listing from provider
  - now, all mission templates retrieved upon provider registration (not only first level)
  - also, when performing a refresh
  - `retrievedAt` with current date for every directory entry, and with `null` for actual template
    (UI uses this information to decide whether to do a complete template load)
  - every mission template entry only with corresponding ID, description and asset classes (no parameters).
  - this enables template selection when creating a new mission
  - UI can request full mission template details:
    - when explicitly selecting a template 
    - when creating a new mission, if template not already loaded

- version 0.9.0
- docker build; no history mode yet; other adjustments
- spa routing and docker preps
- initial (re)implementation of the MXM Server component
