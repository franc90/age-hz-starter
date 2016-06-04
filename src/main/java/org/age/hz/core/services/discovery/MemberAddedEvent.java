package org.age.hz.core.services.discovery;

import com.google.common.base.MoreObjects;
import com.hazelcast.core.EntryEvent;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class MemberAddedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;

    private final NodeId nodeId;

    public MemberAddedEvent(EntryEvent<String, NodeId> event) {
        id = event.getKey();
        nodeId = event.getValue();
    }

    public String getId() {
        return id;
    }

    public NodeId getNodeId() {
        return nodeId;
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
                .add("nodeId", nodeId)
                .toString();
    }
}
