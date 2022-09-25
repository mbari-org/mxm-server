2022-09

- similarly as with `MXM_EXTERNAL_URL`, new env var `MXM_EXTERNAL_WS_URL` that 
  can be used to specify the external URL for the websocket endpoint.
- websocket connection starting to work when deploying server on the mxm machine.
  - more testing ongoing.
  - NOTE: interestingly, `websocat` seems to expose a bug in Quarkus:
    With this invocation:
  
          websocat "ws://mxm.shore.mbari.org/ws/"    # which hangs forever btw
  
      the docker log shows:
  
          ... ERROR [io.qua.ver.cor.run.VertxCoreRecorder] (vert.x-eventloop-thread-0) Uncaught exception received by Vert.x:
             java.lang.NullPointerException: Cannot invoke "String.hashCode()" because "<local5>" is null
          at io.quarkus.smallrye.graphql.runtime.SmallRyeGraphQLOverWebSocketHandler.lambda$doHandle$4(SmallRyeGraphQLOverWebSocketHandler.java:38)
          at io.vertx.ext.web.impl.HttpServerRequestWrapper.lambda$toWebSocket$0(HttpServerRequestWrapper.java:355)
          at io.vertx.core.impl.future.FutureImpl$3.onSuccess(FutureImpl.java:141)
          ...

      No obvious findings out there related with this.

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
