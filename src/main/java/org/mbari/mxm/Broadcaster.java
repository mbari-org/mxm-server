package org.mbari.mxm;

import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

/**
 * Helper for supporting subscriptions.
 */
@Slf4j
public abstract class Broadcaster<T> {

  public abstract String getEntityName();

  public abstract String getPrimaryKey(T t);

  public enum EventType {
    CREATED, UPDATED, DELETED
  }

  public BroadcastProcessor<T> createProcessor(EventType event) {
    var pid = new PID(event);
    return processorMap.computeIfAbsent(pid, k -> {
      log.warn("createProcessor: pid='{}'", pid);
      return BroadcastProcessor.create();
    });
  }

  public BroadcastProcessor<T> createProcessor(EventType event, String primaryKey) {
    var pid = new PID(event, primaryKey);
    return processorMap.computeIfAbsent(pid, k -> {
      log.warn("createProcessor: pid='{}'", pid);
      return BroadcastProcessor.create();
    });
  }

  public void broadcastCreated(T t) {
    broadcastForPID(new PID(EventType.CREATED), t);
  }

  public void broadcastUpdated(T t) {
    broadcast(EventType.UPDATED, t);
  }

  public void broadcastDeleted(T t) {
    broadcast(EventType.DELETED, t);
  }

  private void broadcast(EventType event, T t) {
    broadcastForPID(new PID(event), t);
    broadcastForPID(new PID(event, getPrimaryKey(t)), t);
  }

  private void broadcastForPID(PID pid, T t) {
    var p = processorMap.get(pid);
    if (p != null) {
      p.onNext(t);
    }
  }

  // processors created upon subscriptions:
  private final HashMap<PID, BroadcastProcessor<T>> processorMap = new HashMap<>();

  // Processor ID
  @Value
  private class PID {
    EventType eventType;
    String entityName;
    String primaryKey;

    PID(EventType eventType, String primaryKey) {
      this.eventType = eventType;
      this.entityName = Broadcaster.this.getEntityName();
      this.primaryKey = primaryKey;
    }

    PID(EventType eventType) {
      this.eventType = eventType;
      this.entityName = Broadcaster.this.getEntityName();
      this.primaryKey = null;
    }
  }

}
