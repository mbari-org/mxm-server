package org.mbari.mxm;

import io.vertx.ext.web.Router;
import java.util.LinkedHashMap;
import java.util.Objects;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

// With the mxm-ui component in history mode, part of the following below to
// redirect to / for GET requested paths starting with `/p/` or equal to `/p`.

// https://quarkiverse.github.io/quarkiverse-docs/quarkus-quinoa/dev/#spa-routing
// "Currently, for technical reasons, the Quinoa SPA routing configuration won’t work
// with RESTEasy Classic. Instead, you may use a workaround (if your app has all the
// rest resources under the same path prefix)"

@ApplicationScoped
@Slf4j
public class SPARouting {

  private static final boolean historyMode = false;

  private static final String MXM_EXTERNAL_URL = System.getenv("MXM_EXTERNAL_URL");

  @ConfigProperty(name = "mxm.version")
  String mxmVersion;

  public void init(@Observes Router router) {
    router
        .get("/*")
        .handler(
            rc -> {
              final var path = rc.normalizedPath();

              log.debug("SPARouting: path='{}'", path);

              if (historyMode) {
                // part of some preliminary tests
                if (path.matches("^/p(/.*)?$")) {
                  log.debug("SPARouting: rerouting to / for path='{}'", path);
                  rc.reroute("/");
                  return;
                }
              }

              // provide server related config to the UI:
              if (path.equals("/statics/config/config.json")) {
                final String serverLoc =
                    Objects.requireNonNullElseGet(
                        MXM_EXTERNAL_URL,
                        () -> rc.request().scheme() + "://" + rc.request().host());
                final var graphqlUri = serverLoc + "/graphql";
                log.debug("SPARouting: graphqlUri='{}'", graphqlUri);

                LinkedHashMap<String, Object> config = new LinkedHashMap<>();
                config.put("mxmVersion", mxmVersion);
                var googleApiKey = System.getenv("GOOGLE_API_KEY");
                if (googleApiKey != null) {
                  config.put("googleApiKey", googleApiKey);
                }
                config.put("graphqlUri", graphqlUri);
                config.put("graphqlSchema", serverLoc + "/graphql/schema.graphql");
                config.put("graphqlUi", serverLoc + "/q/graphql-ui");
                config.put("openapi", serverLoc + "/q/openapi");
                config.put("openapiSchema", serverLoc + "/q/openapi");
                config.put("swaggerUi", serverLoc + "/q/swagger-ui");

                final var uiConfig = Utl.writeJson(config);
                rc.response().putHeader("content-type", "application/json").end(uiConfig);
              } else {
                log.debug("SPARouting: calling next() for path='{}' ", path);
                rc.next();
              }
            });
  }
}
