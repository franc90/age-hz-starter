package org.age.hz.core.services.topology;

import org.age.hz.core.services.topology.state.TopologyState;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Optional;
import java.util.Set;

public interface TopologyService {

    void internalStart();

    void internalStop();

    void electMaster();

    void setState(TopologyState state);

    void topologyChanged();

    void topologyConfiguredByMaster();

    TopologyState getState();

    Optional<String> getMasterId();

    boolean isLocalNodeMaster();

    boolean hasTopology();

    Optional<DirectedGraph<String, DefaultEdge>> getTopologyGraph();

    int getNodesCount();

    Set<String> getNodes();

    Set<String> getNeighbours();

    void handleError();
}
