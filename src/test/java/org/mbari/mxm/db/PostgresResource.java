package org.mbari.mxm.db;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.mbari.mxm.db.support.DbSupport;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

@Slf4j
public class PostgresResource implements QuarkusTestResourceLifecycleManager {

  // TODO this could probably be simpler/more elegant, but it's working for now
  private static PostgreSQLContainer<?> createPgContainer() {
    var container =
        new PostgreSQLContainer<>("postgres:14")
            .withDatabaseName("mxm_test")
            .withUsername("mxm")
            .withPassword("mxm")
            .withAccessToHost(true);

    // copy schema and data scripts:
    container = copyFileToContainer(container, "mxm-00-schema.sql");
    container = copyFileToContainer(container, "mxm-10-data-units.sql");
    container = copyFileToContainer(container, "mxm-20-data-assetclasses.sql");
    container = copyFileToContainer(container, "mxm-25-data-assets.sql");
    container = copyFileToContainer(container, "mxm-25-data-assets-tethysdash.sql");
    return container;
  }

  private static PostgreSQLContainer<?> copyFileToContainer(PostgreSQLContainer<?> c, String name) {
    return c.withCopyFileToContainer(
        MountableFile.forHostPath("docker/" + name), "/docker-entrypoint-initdb.d/" + name);
  }

  static PostgreSQLContainer<?> db = null;

  @Override
  public Map<String, String> start() {
    if (db == null) {
      db = createPgContainer();
    }
    db.start();
    log.info("PostgresResource started: url='{}' running={}", db.getJdbcUrl(), db.isRunning());

    DbSupport.setJdbcUrl(db.getJdbcUrl(), db.getUsername(), db.getPassword());

    // but, how can we access this later on if we wanted to use it instead of the above?
    return Collections.singletonMap("my.jdbcUrl", db.getJdbcUrl());
  }

  @Override
  public void stop() {
    log.info("PostgresResource stopping db={}", db);
    if (db != null) {
      db.stop();
      db = null;
    }
  }
}
