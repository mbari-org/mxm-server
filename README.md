# Mission Execution Mediation Service

**WIP**

The Mission Execution Mediation Service (MXM) effort seeks to provide a set of
programmatic and user interfaces to integrate mission information across
diverse mission execution systems at MBARI, as well as to support the integration
of third-party applications, in particular to facilitate extended planning
capabilities on MBARI assets

The proposed MXM interfaces will support a unified view of the information in terms
of available mission definitions, parameterization, scheduling, and execution status.

## The MXM ecosystem

The MXM ecosystem consists of the following components:

- MXM Server: the central MXM service where mission execution systems (_providers_) 
  can be registered to expose all relevant mission information and capabilities for
  mission scheduling.
- MXM Webapp: The GUI for the MXM service.
- Providers: The external mission execution systems integrated into the MXM ecosystem.
  Each provider implements an MXM Provider API (in full or in part, depending on capabilities)
  to support this integration.

## MXM Server

This repo is a [Quarkus](https://quarkus.io/)-based implementation of the MXM Server component.

- Postgres database.
- GraphQL API to support all functionality of the MXM system,
  that is, extensively by the MXM Webapp,
  but, eventually, also for some specific operations that are initiated by the integrated providers.
- Interaction with registered providers using the MXM Provider REST API.

## Development

Along with your [preferred IDE](https://quarkus.io/guides/ide-tooling),
just a typical Quarkus development workflow:

```
quarkus dev
```

- Type 'r' to run tests ('f' to only run failed tests..)
- Type 'd' to open Dev UI in your browser (from which to open GraphQL UI and other tools)
- Etc.

### Quinoa

Supported by the [Quarkus Quinoa extension](https://github.com/quarkiverse/quarkus-quinoa),
this server is able to launch the UI (mxm-ui) component both during development
(ie., `quarkus dev` also launches `quasar dev`) and in production.
In both cases, the UI code is assumed located at `../mxm-ui`.

### Contributing

Make sure all tests are passing and also run `./mvnw spotless:apply` before committing.

## Initial setup notes

Some initial setup notes [here](misc/initial-setup.md).
