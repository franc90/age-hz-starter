package org.age.hz.core.services.topology;

import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;
import org.age.hz.core.services.AbstractService;
import org.age.hz.core.services.topology.leader.election.LeaderElector;
import org.age.hz.core.services.topology.messages.MasterElectedMessage;
import org.age.hz.core.services.topology.messages.TopologyMessage;
import org.age.hz.core.services.topology.messages.TopologySelectedMessage;
import org.age.hz.core.services.topology.processor.TopologyProcessor;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Named
public class TopologyServiceImpl extends AbstractService implements SmartLifecycle, TopologyService {

    private static final Logger log = LoggerFactory.getLogger(TopologyServiceImpl.class);

    @Inject
    private MessageListener<TopologyMessage> topologyUpdatesListener;

    @Inject
    private LeaderElector leaderElector;

    @Inject
    private TopologyProcessor topologyProcessor;

    private ITopic<TopologyMessage> topologyUpdatesTopic;

    private IMap<String, Object> topologyConfigurationMap;

    private DirectedGraph<String, DefaultEdge> cachedTopologyGraph;

    private TopologyState state = TopologyState.OFFLINE;

    @PostConstruct
    private void init() {
        topologyUpdatesTopic = hazelcastInstance.getTopic("topology/channel");
        topologyConfigurationMap = hazelcastInstance.getMap("topology/configuration");
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        log.debug("Stop topology service with callback");
        stop();
        callback.run();
    }

    @Override
    public void start() {
        log.debug("Start topology service");
        eventBus.post(TopologyEvent.START);
    }

    @Override
    public void stop() {
        log.debug("Stop topology service");
        eventBus.post(TopologyEvent.STOP);
    }

    @Override
    public boolean isRunning() {
        boolean notInitialState = state != TopologyState.OFFLINE;
        boolean notTerminalState = state != TopologyState.TERMINATED && state != TopologyState.FAILED;
        return notInitialState && notTerminalState;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    public TopologyState getState() {
        return state;
    }

    @Override
    public void setState(TopologyState state) {
        this.state = state;
    }

    @Override
    public void internalStart() {
        log.debug("Starting topology service");

        topologyUpdatesTopic.addMessageListener(topologyUpdatesListener);
        eventBus.register(this);

        log.debug("Topology service started.");
        eventBus.post(TopologyEvent.STARTED);
    }

    @Override
    public void internalStop() {
        log.debug("Stopping topology service");

        log.debug("Topology service stopped.");
    }

    @Override
    public void electMaster() {
        log.debug("Selecting master locally");

        try {
            leaderElector.electLeader();
            if (leaderElector.isCurrentNodeMaster()) {
                configureAsMaster();
            } else {
                configureAsSlave();
            }
        } catch (Throwable throwable) {
            log.error("Could not elect leader, shutting down. {}", throwable);
            eventBus.post(TopologyEvent.ERROR);
        }
    }

    private void configureAsMaster() {
        topologyConfigurationMap.put(TopologyConfigKeys.MASTER_NODE_ID, myId);

        state = TopologyState.MASTER;
        eventBus.post(TopologyEvent.TOPOLOGY_CHANGED);
        topologyUpdatesTopic.publish(new MasterElectedMessage());
    }

    private void configureAsSlave() {
        state = TopologyState.SLAVE;
    }

    /**
     * Executed only on master, called when:
     * * new member
     * * member removed
     */
    public void topologyChanged() {
        if (!leaderElector.isCurrentNodeMaster()) {
            log.debug("I am not a master - therefore I do not change topology.");
            return;
        }

        cachedTopologyGraph = topologyProcessor.createTopologyGraph();
        log.debug("New topology: {}", cachedTopologyGraph);
        topologyConfigurationMap.put(TopologyConfigKeys.TOPOLOGY_GRAPH, cachedTopologyGraph);
        topologyUpdatesTopic.publish(new TopologySelectedMessage());
    }

    public void topologyConfiguredByMaster() {
        if (getCurrentTopologyGraph() == null) {
            throw new IllegalStateException("No topology graph in config");
        }

        log.debug("Get configured topology");
        cachedTopologyGraph = getCurrentTopologyGraph();
        log.debug("New cached topology size: {}", cachedTopologyGraph.vertexSet().size());
        log.debug("New cached topology: {}", cachedTopologyGraph);
    }

    @SuppressWarnings("unchecked")
    private DirectedGraph<String, DefaultEdge> getCurrentTopologyGraph() {
        return (DirectedGraph<String, DefaultEdge>) topologyConfigurationMap.get(TopologyConfigKeys.TOPOLOGY_GRAPH);
    }

    @Override
    public Optional<String> getMasterId() {
        return Optional.ofNullable((String) topologyConfigurationMap.get(TopologyConfigKeys.MASTER_NODE_ID));
    }

    @Override
    public boolean isLocalNodeMaster() {
        return leaderElector.isCurrentNodeMaster();
    }

    @Override
    public boolean hasTopology() {
        return state == TopologyState.WITH_TOPOLOGY;
    }

    @Override
    public Optional<DirectedGraph<String, DefaultEdge>> getTopologyGraph() {
        return Optional.ofNullable(cachedTopologyGraph);
    }

    @Override
    public Set<String> getNeighbours() {
        if (!hasTopology()) {
            throw new IllegalStateException("Topology is not ready yet.");
        }

        DirectedGraph<String, DefaultEdge> topologyGraph = getCurrentTopologyGraph();
        Set<DefaultEdge> outEdges = topologyGraph.outgoingEdgesOf(myId.getNodeId());
        return outEdges.stream().map(topologyGraph::getEdgeTarget).collect(Collectors.toSet());
    }

    @Override
    public void handleError() {

    }
}