package org.mbari.mxm.db;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.Update;

import java.util.ArrayList;
import java.util.List;

public class DbUtl {
  static String snakize(String s) {
    return s.replaceAll("([A-Z])", "_$1").toLowerCase();
  }

  public static CreateDef createDef(String tableName) {
    return new CreateDef(tableName);
  }

  public static class CreateDef {
    final String tableName;
    final List<String> cols = new ArrayList<>();
    final List<String> vals = new ArrayList<>();

    public CreateDef(String tableName) {
      this.tableName = tableName;
    }

    public CreateDef set(Object fieldValue, String camelName) {
      if (fieldValue != null) {
        cols.add(snakize(camelName));
        vals.add(":" + camelName);
      }
      return this;
    }

    public Update createUpdate(Handle handle) {
      var sql = """
        insert into <table>
        (<cols>) values (<vals>)
        returning *
        """;
      return handle.createUpdate(sql)
        .define("table", tableName)
        .defineList("cols", cols)
        .defineList("vals", vals)
        ;
    }
  }

  public static UpdateDef updateDef(String tableName, Object pl) {
    return new UpdateDef(tableName, pl);
  }

  public static class UpdateDef {
    final String tableName;
    final Object pl;
    private final List<String> ands = new ArrayList<>();
    private final List<String> sets = new ArrayList<>();

    private UpdateDef(String tableName, Object pl) {
      this.tableName = tableName;
      this.pl = pl;
    }

    private static String set(String camelName) {
      return snakize(camelName) + " = :" + camelName;
    }

    public UpdateDef where(String camelName) {
      ands.add(set(camelName));
      return this;
    }

    public UpdateDef set(Object fieldValue, String camelName) {
      if (fieldValue != null) {
        sets.add(set(camelName));
      }
      return this;
    }

    public UpdateDef setEvenIfNull(Object fieldValue, String camelName) {
      if (fieldValue != null) {
        return set(fieldValue, camelName);
      }
      else {
        return setNull(camelName);
      }
    }

    public UpdateDef setNull(String camelName) {
      sets.add(snakize(camelName) + " = null");
      return this;
    }

    public boolean noSets() {
      return sets.isEmpty();
    }

    // mainly for debugging purposes
    public List<String> getSets() {
      return sets;
    }

    public Query createQuery(Handle handle) {
      var sql = """
        update <table>
        set    <sets>
        where  <ands>
        returning *
        """;
      return handle.createQuery(sql)
        .define("table", tableName)
        .defineList("sets", sets)
        .define("ands", String.join(" and ", ands))
        .bindBean(pl)
        //.defineNamedBindings() // unneeded for now as we are not using the `<if(a)>a = :a,<endif>` syntax.
        ;
    }
  }
}
