package org.age.hz.core.services.topology.processor;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public interface TopologyProcessor {

    DirectedGraph<String, DefaultEdge> createTopologyGraph();

}
