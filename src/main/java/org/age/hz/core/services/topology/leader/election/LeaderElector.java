package org.age.hz.core.services.topology.leader.election;

import org.age.hz.core.node.NodeId;

public interface LeaderElector {

    NodeId electLeader() throws Throwable;

    boolean isCurrentNodeMaster();

}
