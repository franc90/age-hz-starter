package org.age.hz.core.services.topology.processor;

import org.age.hz.core.services.discovery.DiscoveryService;
import org.age.hz.core.node.NodeId;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UnmodifiableDirectedGraph;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.getLast;

@Named
public class RingTopologyProcessor implements TopologyProcessor {

    private final DiscoveryService discoveryService;

    @Inject
    public RingTopologyProcessor(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    public DirectedGraph<String, DefaultEdge> createTopologyGraph() {
        final DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        Set<NodeId> allMembers = discoveryService.getAllMembers();
        allMembers.forEach(identity -> graph.addVertex(identity.getNodeId()));

        final List<String> sortedIds = allMembers.stream()
                .map(NodeId::getNodeId)
                .sorted()
                .collect(Collectors.toList());

        sortedIds.stream().reduce(getLast(sortedIds), (nodeIdentity1, nodeIdentity2) -> {
            graph.addEdge(nodeIdentity1, nodeIdentity2);
            return nodeIdentity2;
        });

        return new UnmodifiableDirectedGraph<>(graph);
    }

}
