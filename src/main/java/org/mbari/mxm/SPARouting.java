package org.mbari.mxm;

import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.Map;

// With the mxm-ui component in history mode, part of the following below to
// redirect to / for GET requested paths starting with `/p/` or equal to `/p`.

// https://quarkiverse.github.io/quarkiverse-docs/quarkus-quinoa/dev/#spa-routing
// "Currently, for technical reasons, the Quinoa SPA routing configuration won’t work
// with RESTEasy Classic. Instead, you may use a workaround (if your app has all the
// rest resources under the same path prefix)"

@ApplicationScoped
@Slf4j
public class SPARouting {

  public void init(@Observes Router router) {
    router.get("/*").handler(rc -> {
      final var path = rc.normalizedPath();

      log.warn("SPARouting: path='{}'", path);

      // SPA routing for history mode:
      if (path.matches("^/p(/.*)?$")) {
        log.debug("SPARouting: rerouting to / for path='{}'", path);
        rc.reroute("/");
      }

      // TODO adjust to provide server related config to the UI:
      else if (path.equals("/statics/config/config.json")) {
        final var serverLoc = rc.request().scheme() + "://" + rc.request().host();
        final var graphqlUri = serverLoc + "/graphql";
        log.debug("SPARouting: graphqlUri='{}'", graphqlUri);
        final var uiConfig = Utl.writeJson(Map.of(
          "graphqlUri", graphqlUri
          // other props ...
        ));
        rc.response().putHeader("content-type", "application/json")
          .end(uiConfig)
        ;
      }

      else {
        log.debug("SPARouting: calling next() for path='{}' ", path);
        rc.next();
      }
    });
  }
}
