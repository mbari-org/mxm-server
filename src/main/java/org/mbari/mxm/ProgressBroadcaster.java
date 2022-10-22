package org.mbari.mxm;

import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import java.util.HashMap;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/** Helper for reporting progress of some kind. */
@Slf4j
public abstract class ProgressBroadcaster<T> {

  public abstract String getEntityName();

  public abstract String getPrimaryKey(T t);

  public BroadcastProcessor<T> createProcessor(String primaryKey) {
    var pid = new PID(primaryKey);
    return processorMap.computeIfAbsent(
        pid,
        k -> {
          log.warn("createProcessor: pid='{}'", pid);
          return BroadcastProcessor.create();
        });
  }

  public void broadcast(T t) {
    var pid = new PID(getPrimaryKey(t));
    var p = processorMap.get(pid);
    log.trace("broadcast: pid='{}' p={}", pid, p);
    if (p != null) {
      p.onNext(t);
    }
  }

  // processors created upon subscriptions:
  private final HashMap<PID, BroadcastProcessor<T>> processorMap = new HashMap<>();

  @Value
  private class PID {
    String entityName;
    String primaryKey;

    PID(String primaryKey) {
      this.entityName = ProgressBroadcaster.this.getEntityName();
      this.primaryKey = primaryKey;
    }
  }
}
