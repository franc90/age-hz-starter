package org.age.hz.core.services.discovery;

import java.util.Optional;
import java.util.Set;

public interface DiscoveryService {

    Set<NodeId> getAllMembers();

    Optional<NodeId> getMember(NodeId id);

    void cleanUp();

}
