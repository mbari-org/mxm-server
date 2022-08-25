## Misc notes

### Native

Native build is not a priority, but did some quick testing:
```
$ ./mvnw package -Pnative
...
$ target/mxm-1.0.0-SNAPSHOT-runner
...
2022-08-25 09:10:32,510 INFO  [io.quarkus] (main) mxm 1.0.0-SNAPSHOT native (powered by Quarkus 2.11.3.Final) started in 0.153s. Listening on: http://0.0.0.0:8085
2022-08-25 09:10:32,511 INFO  [io.quarkus] (main) Profile prod activated.
2022-08-25 09:10:32,511 INFO  [io.quarkus] (main) Installed features: [cdi, jdbc-postgresql, quinoa, rest-client, resteasy, resteasy-jackson, smallrye-context-propagation, smallrye-graphql, smallrye-openapi, vertx]
```
webapp <http://localhost:8085/> loads OK, but then, unsurprisingly, getting errors related with JDBI:
```
 SRGQL012000: Data Fetching Error: java.lang.IllegalStateException: Unable to instantiate config class class org.jdbi.v3.core.config.JdbiCaches. Is there a public no-arg constructor?
	at org.jdbi.v3.core.config.ConfigRegistry.lambda$configFactory$4(ConfigRegistry.java:105)
```
see <https://github.com/jdbi/jdbi/issues/1797>. While JDBI provides a proper solution, a possible workaround would be
to use `quarkus.native.additional-build-args` along with GraalVM native-image config files (as done for TethysL).
(A quarkus+jdbi example: <https://gist.github.com/Eng-Fouad/7b5925481dd391fcc74487a68484b987>).

Btw, possibly unrelated, but also got multiple of these warnings:
```
WARN  [io.sma.gra.exe.eve.EventEmitter] (vert.x-eventloop-thread-8) Failed to register io.smallrye.graphql.spi.EventingService: Provider io.smallrye.graphql.cdi.tracing.TracingService not found
```

### Tools

#### Hasura gq (GraphQurl)

<https://github.com/hasura/graphqurl>

```
npm install -g graphqurl

$ gq http://localhost:8085/graphql \
     -q '{ allProviders { providerId httpEndpoint description} }' | jq .
```

To open GraphiQL interface including an Explorer:

```
gq http://localhost:8085/graphql -i
```

(The Quarkus based GraphiQL UI doesn't provide such explorer feature.)

#### curlie

<https://til.simonwillison.net/graphql/graphql-with-curl>

```
$ curlie post http://localhost:8085/graphql -s \
-d "$(jq -c -n --arg query '
{
   allProviders {
     providerId
     httpEndpoint
   }
}' '{"query":$query}')"
```
