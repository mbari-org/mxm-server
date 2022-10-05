package org.mbari.mxm.db.support;

import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

@ApplicationScoped
@Slf4j
public class DbSupport {

  private static final String defaultJdbcUrl = "jdbc:postgresql://localhost:5432/mxm";

  Jdbi jdbi = null;

  // defaults for a regular, if ad hoc at the moment, "prod" environment:
  static String jdbcUrl = System.getenv().getOrDefault("JDBC_URL", defaultJdbcUrl);
  static String username = System.getenv("POSTGRES_USER");
  static String password = System.getenv("POSTGRES_PASSWORD");

  public static void setJdbcUrl(String url, String u, String p) {
    log.warn("setJdbcUrl: url='{}'", url);
    jdbcUrl = url;
    username = u;
    password = p;
  }

  public Jdbi getJdbi() {
    if (jdbi == null) {
      log.debug("DbSupport: jdbcUrl='{}' username='{}'", jdbcUrl, username);
      if (username != null && password != null) {
        jdbi = Jdbi.create(jdbcUrl, username, password);
      } else {
        jdbi = Jdbi.create(jdbcUrl);
      }
      jdbi.installPlugin(new PostgresPlugin());
      jdbi.installPlugin(new SqlObjectPlugin());
    }
    return jdbi;
  }
}
