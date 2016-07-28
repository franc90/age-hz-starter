package org.age.hz.core.services.discovery.events;

import com.google.common.base.MoreObjects;
import com.hazelcast.core.EntryEvent;
import org.age.hz.core.node.NodeId;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class MemberAddedEvent implements MembershipChangedEvent {

    private static final long serialVersionUID = 1L;

    private final String id;

    private final NodeId nodeId;

    public MemberAddedEvent(EntryEvent<String, NodeId> event) {
        id = event.getKey();
        nodeId = event.getValue();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MemberAddedEvent that = (MemberAddedEvent) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(nodeId, that.nodeId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(nodeId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("myId", nodeId)
                .toString();
    }
}
