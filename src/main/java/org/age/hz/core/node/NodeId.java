package org.age.hz.core.node;

import com.google.common.base.MoreObjects;
import org.age.hz.core.services.discovery.DiscoveryServiceImpl;
import org.age.hz.core.utils.TimeUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import java.io.Serializable;
import java.util.UUID;

@Named
public class NodeId implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(NodeId.class);

    private static final long serialVersionUID = 1L;

    private final String nodeId = UUID.randomUUID().toString();

    @PostConstruct
    public void init() {
        long timestamp = System.currentTimeMillis();
        log.warn("{},ini,{},{}", TimeUtils.toString(timestamp), timestamp, nodeId);
    }

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
                .add("myId", nodeId)
                .toString();
    }
}
