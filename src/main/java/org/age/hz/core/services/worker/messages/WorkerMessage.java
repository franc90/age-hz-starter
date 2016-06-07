package org.age.hz.core.services.worker.messages;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import org.age.hz.core.node.NodeId;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Consumer;

public abstract class WorkerMessage<T extends Serializable> implements Consumer<EventBus>, Serializable {

    protected final Set<String> recipients;

    protected final boolean broadcast;

    protected final T payload;

    WorkerMessage(T payload) {
        this.payload = payload;
        recipients = ImmutableSet.of();
        broadcast = true;
    }

    WorkerMessage(Set<String> recipients, T payload) {
        this.recipients = ImmutableSet.copyOf(recipients);
        this.payload = payload;
        broadcast = false;
    }

    WorkerMessage(Set<String> recipients, T payload, boolean broadcast) {
        this.recipients = ImmutableSet.copyOf(recipients);
        this.payload = payload;
        this.broadcast = broadcast;
    }

    public boolean isNotRecipient(NodeId id) {
        return isRecipient(id);
    }

    private boolean isRecipient(NodeId id) {
        return broadcast || recipients.contains(id.getNodeId());
    }

    public Set<String> getRecipients() {
        return recipients;
    }

    public T getPayload() {
        return payload;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("recipients", recipients)
                .add("broadcast", broadcast)
                .toString();
    }
}
