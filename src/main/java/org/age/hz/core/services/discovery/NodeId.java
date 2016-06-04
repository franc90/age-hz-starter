package org.age.hz.core.services.discovery;

import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.inject.Named;
import java.io.Serializable;
import java.util.UUID;

@Named
public class NodeId implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nodeId = UUID.randomUUID().toString();

    public String getNodeId() {
        return nodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        NodeId nodeId1 = (NodeId) o;

        return new EqualsBuilder()
                .append(nodeId, nodeId1.nodeId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(nodeId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("nodeId", nodeId)
                .toString();
    }
}
