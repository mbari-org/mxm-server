package org.mbari.mxm.db;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.db.support.DbSupport;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;

@Slf4j
public class PostgresResource implements QuarkusTestResourceLifecycleManager {

  // TODO this could probably be simpler/more elegant, but it's working for now
  static PostgreSQLContainer<?> db =
      new PostgreSQLContainer<>("postgres:14")
          .withDatabaseName("mxm_test")
          .withUsername("mxm")
          .withPassword("mxm")
          .withAccessToHost(true)
          .withClasspathResourceMapping(
              "mxm-schema.sql", "/docker-entrypoint-initdb.d/init.sql", BindMode.READ_ONLY)
          .withClasspathResourceMapping(
              "mxm-data.sql", "/docker-entrypoint-initdb.d/mxm-data.sql", BindMode.READ_ONLY)
      //      .waitingFor(new org.testcontainers.containers.wait.strategy.HttpWaitStrategy()
      //        .forStatusCode(200)
      //        .withStartupTimeout(java.time.Duration.ofSeconds(5))
      //        )

      ;

  @Override
  public Map<String, String> start() {
    db.start();
    log.info("PostgresResource started: url='{}' running={}", db.getJdbcUrl(), db.isRunning());

    DbSupport.setJdbcUrl(db.getJdbcUrl(), db.getUsername(), db.getPassword());

    // but, how can we access this later on if we wanted to use it instead of the above?
    return Collections.singletonMap("my.jdbcUrl", db.getJdbcUrl());
  }

  @Override
  public void stop() {
    log.info("PostgresResource stopping");
    db.stop();
  }
}
