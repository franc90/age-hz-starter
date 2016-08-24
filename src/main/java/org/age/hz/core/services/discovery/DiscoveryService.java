package org.age.hz.core.services.discovery;

import org.age.hz.core.node.NodeId;
import org.springframework.context.SmartLifecycle;

import java.util.Optional;
import java.util.Set;

public interface DiscoveryService extends SmartLifecycle {

    Set<NodeId> getAllMembers();

    Optional<NodeId> getMember(NodeId id);

    void cleanUp();

}
