package org.age.hz.core.services.topology.leader.election;

import org.age.hz.core.services.discovery.DiscoveryService;
import org.age.hz.core.node.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

@Named
public class SimpleLeaderElector implements LeaderElector {

    private static final Logger log = LoggerFactory.getLogger(SimpleLeaderElector.class);

    @Inject
    private DiscoveryService discoveryService;

    @Inject
    private NodeId myId;

    private boolean master;

    public NodeId electLeader() throws Throwable {
        Set<NodeId> nodes = discoveryService.getAllMembers();
        Optional<NodeId> maxId = nodes.parallelStream().max(Comparator.comparing(NodeId::getNodeId));

        NodeId masterId = maxId.orElseThrow(() -> {
            log.error("Could not elect leader - no nodes");
            throw new IllegalStateException("No nodes to elect leader from");
        });

        log.debug("Max id is {}.", masterId);

        if (myId.getNodeId().equals(masterId.getNodeId())) {
            log.debug("I am the master");
            master = true;
        } else {
            log.debug("I am a slave");
            master = false;
        }

        return masterId;
    }

    public boolean isCurrentNodeMaster() {
        return master;
    }

}
