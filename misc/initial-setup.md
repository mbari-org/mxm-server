Some initial setup notes (in no particular order):

- Using Testcontainers, so no need to have a database in place for the tests.
- However, a database is assumed in place for the development and production profiles.
  TODO: streamline this.
- Initially, with synchronous (blocking) access to the database.
  (Asynchronous, non-blocking access may be tackled later on as needed.)
- Some subscriptions implemented but actual need/use in GUI/clients still TBD.
- [JDBI](http://jdbi.org/) used for the database access. This initial choice mainly made because the
  database schema has already been in place for some time, the queries/mutations are in general
  uncomplicated, and to avoid dealing with potential idiosyncrasies with some ORM on top of it.
- GraphQL API focused on provider registration, mission submission, and general queries.
- Provider related entities are directly populated by fetching them from the provider at time
  of registration or upon a "refresh" mutation.
- A "refresh" mutation is one where only the primary key of the relevant entity
  (say, a mission template) is given by the GUI/client, and the server proceeds with
  the actual updates by querying the provider and reflecting the changes in the database.
- Preliminary error handling via GraphQLException and with ping request to provider.
- The ping request is mainly intended to support quick connection tests to a provider,
  in particular, prior to be registered, but in any other cases as convenient.
- Some generalized handling of event kinds in broadcast support for subscriptions.
- provider_client package with:
  - Interface via REST MXM Provider API (already functional but still being defined in general)
  - Such API has been implemented by TethysDash and the front tracking for wave gliders. 
  - Interaction with external provider (e.g., upon a createProvider request)
  - Basic, on-demand mission template listing.
- In general, using the entity classes themselves as mutation inputs for the GraphQL schema.
- Use of testcontainers for db+jdbi.
  It seems there will be a [quarkus-jdbi extension](https://github.com/quarkiverse/quarkus-jdbi)
  sometime in the future, which hopefully will make things simpler to set up in general.
- Production profile still ad hoc at the moment, assuming database in place.
- In insertions, using `returning *` and jdbi's `@RegisterBeanMapper()` to get the created entity.
- jdbi: how to map sql errors to provide better user-level error messages (eg, upon a duplicate key error)?
- does SmallRye-Graphql provide more powerful query capabilities?
- wrapper classes to do field extensions (while avoiding erasure limitation)
  (E.g., `assets` in `Provider` and `AssetClasses` graphql models.)
- Added some JAX-RS resources for a preliminary REST API <http://localhost:8080/q/swagger-ui/>.
  Such REST API for the MXM service itself is not really the initial focus at the moment, but this will
  be revisited when refining the interaction _from_ providers (for example, for a simple notification
  mechanism for certain events like "new data generated", etc.)
