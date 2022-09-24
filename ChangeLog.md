2022-09

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
