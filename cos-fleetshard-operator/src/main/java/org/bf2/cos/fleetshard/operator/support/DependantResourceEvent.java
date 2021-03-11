package org.bf2.cos.fleetshard.operator.support;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.Watcher;
import io.javaoperatorsdk.operator.processing.event.AbstractEvent;
import io.javaoperatorsdk.operator.processing.event.EventSource;

public class DependantResourceEvent<T> extends AbstractEvent {
    private final Watcher.Action action;
    private final T resource;

    public DependantResourceEvent(
            Watcher.Action action,
            T resource,
            String ownerUid,
            EventSource eventSource) {

        super(ownerUid, eventSource);

        this.action = action;
        this.resource = resource;
    }

    public DependantResourceEvent(
            Watcher.Action action,
            T resource,
            HasMetadata hasMetadata,
            EventSource eventSource) {

        this(action, resource, hasMetadata.getMetadata().getOwnerReferences().get(0).getUid(), eventSource);
    }

    public Watcher.Action getAction() {
        return action;
    }

    public T getResource() {
        return resource;
    }
}