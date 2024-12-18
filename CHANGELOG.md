2024-11

0.9.95

- Updated docker instructions, and tested dockerized system locally.
- Quinoa:
    - Updated quinoa to 2.3.10 (from 1.2.6).
      Per https://github.com/quarkiverse/quarkus-quinoa, that's the version for Quarkus 3.8.
    - Note: Make sure to use node 18 (e.g., `nvm install 18 && nvm use 18`) before launching
      `quarkus dev` due to the UI (`../mxm-ui`) still requiring it.
      I've had node 22 by default in general for a while, and got quinoa related errors.

2024-03

0.9.94

- upgrade quarkus to 3.8.2, and other base dependencies
- upgrade quarkus to 3.1.1 (from 2.16.4)

0.9.92

- enabled quarkus-micrometer-registry-prometheus

2023-09

0.9.91

- bump quarkus to 2.16.11
- bump wiremock-jre8 to 3.0.1. "All 25 tests are passing (0 skipped)"
  (motivated by https://github.com/mbari-org/mxm-server/security/dependabot/1)

---

2023-05

- CD now triggered via watchtower --http-api-update option.
  (But minimal adjustments here; the triggering is done under 'mxm' repo.)
  
2023-04

0.9.90

- upgrade quinoa to 1.2.6
- upgrade quarkus to 2.16.6 (and CORS related adjustment)

---

2022-12

- some dependency upgrades (upon reviewing corresp release notes)
- upgrade quarkus to 2.15.0 (no need for migrations)

2022-11

- upgrade quarkus to 2.14.0 (no need for migrations)
- upgrade quarkus to 2.13.4
- create status updates upon mission submission
- ping request now with MxmInfo payload (`POST /mxmInfo` removed in Provider API)

2022-10

- discovered `/`-encoding related issue when the provider makes requests to the MXM service
  on `mxm`, which involves an apache proxy pass. In concrete, our missionTemplateId's have `/`,
  which are in general to be transparently encoded/decoded by requester and server; however,
  this gets broken by the proxy pass. Example, from my computer:
  ```
  http put 'http://mxm.shore.mbari.org/providers/provider-example@mxm/missionTemplates/%2FmtA.tl/missions/AAAA/status' Content-type:application/json status=RUNNING
  ```
  returns:
  ```
  HTTP/1.1 404 Not Found`
  Server: Apache/2.4.6
  ...
  The requested URL /providers/provider-example@mxm/missionTemplates//mtA.tl/missions/AAAA/status was not found on this server
  ```
  Quickly tried `ProxyPass / http://localhost:8080/ nocanon` but this didn't seem to make any difference.
  So, workaround is for the requester to replace `/` for `:` in the missionTemplateId PathParam,
  and for MXM to recover the '/' prior to further processing.

    NOTE: The following apache settings ended up working so the encoded slashes do reach the MXM service:
    ```apache
    AllowEncodedSlashes NoDecode
    ProxyPass /  http://localhost:8080/  nocanon
    ```
    Conclusion:
  
    - I'll keep those adjusted apache settings but also the workaround for the time being.
      Revisit this at some point.
      (`nocanon` seems discouraged in general; also see https://stackoverflow.com/a/4443129/830737)
    - Would of course need to document this to provider implementors if sticking with the workaround. 
 
- build/push postgres image 0.9.82
- broadcast mission status updates as they are reported from provider
  - TODO(low prio): also upon mission submission itself
- added MissionStatusUpdate model and initial associated handling
  - extended field `missionStatusUpdates` for `Mission` (graphql)
  - TODO subscription seems to be working but not completely(?)
  - TODO update postgres image with the new table
- MissionStatus with list of statusUpdates
- REST API revision to reflect intended use only by external provider to:
  - register/update provider
  - create/update mission templates 
  - create/update missions
  - report mission status
      - TODO continue revising mission status handling

- note edits re `mbari/mxm` image build and CD (watchtower) with major-minor tag

- noting BackPressureFailure as follows:
  - in GraphQL UI, have an active `missionUpdated` subscription including `arguments` member:
      ```graphql
      subscription missionUpdated {
        missionUpdated {
          updatedDate
          arguments {
            paramName
            paramValue
          }
        }
      }
      ```
  - in mxm-ui, do any argument change
  - exception reported on the quarkus console:
      ```
      ERROR [io.sma.graphql] (..) SRGQL012000: Data Fetching Error:
               io.smallrye.mutiny.subscription.BackPressureFailure:
                    Could not emit item downstream due to lack of requests
      ```
    
  Note that all goes OK above if the `arguments` member is not included in the subscription.
  Maybe some lack of backpressure handling in GraphQL UI?
  I say "reported" because the stacktrace is printed out by the library, not
  exposed to the caller of `onNext` (in Broadcaster).
  I wanted to add a try/catch for a more graceful display but apparently there's no way.
  TODO(low prio) revisit this at some point.

- db: although *derived*, adding for convenience couple units as *base* that some LRAUV missions use
  This helps avoid the associated constraint violation. 
- handle missionStatus reported from provider
- external provider can now "asynchronously" notify the service about events
  - this is based on new MxmInfo that is indicated to provider upon registration
  - provider uses the associated endpoint to notify the service as desired
    (for now, for basic mission status updates)
  - TODO more organized handling, perhaps a common endpoint for this purpose..
- performance fix in refresh of "directory mission template": the operation was taking way longer than
  expected and this was because the detailed info per template was also being loaded, but only the basic
  description is needed.  Only when a proper template is refreshed is when all the details are needed.
- report progress during mission template(s) update
- new subscription to report progress during provider registration
- new `ProviderCreate` as proper payload for provider registration
- moved `For*` classes to a separate package `ext` ("extended fields")
- bump jdbi version to 3.34.0
- read version from pom.xml; update quarkus to 2.13.3.Final
- fixed query for getNumAssetClassesMultipleProviders
- update quarkus to 2.13.2.Final and quinoa to 1.2.2
- more efficient query for `Provider`s `numAssetClasses` resolution
- more consistent route naming: `GET missions/{missionId: .*}` - 
- capture members upon mutation updateMissionTemplate (for actual template)
- add `MxmException` class toward improved error handling, in particular when interacting
  with external providers, such as for connection issues or db violations, etc.

- add `missionTemplates` and `providers` member resolution for `AssetClass` (graphql)
  - with more efficient dispatch depending on selected fields
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
