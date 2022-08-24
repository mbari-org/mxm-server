package org.mbari.mxm.db.support;

import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Slf4j
public class DbSupport {

  Jdbi jdbi = null;

  // defaults for a regular, if ad hoc at the moment, "prod" environment:
  static String jdbcUrl = "jdbc:postgresql://localhost:5432/mxm";
  static String username;
  static String password;

  public static void setJdbcUrl(String url, String u, String p) {
    jdbcUrl = url;
    username = u;
    password = p;
  }

  public Jdbi getJdbi() {
    if (jdbi == null) {
      log.debug("DbSupport: jdbcUrl='{}'", jdbcUrl);
      if (username != null && password != null) {
        jdbi = Jdbi.create(jdbcUrl, username, password);
      }
      else {
        jdbi = Jdbi.create(jdbcUrl);
      }
      jdbi.installPlugin(new PostgresPlugin());
      jdbi.installPlugin(new SqlObjectPlugin());
    }
    return jdbi;
  }
}
