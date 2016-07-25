package org.age.hz.core.services.worker;

import com.google.common.base.Optional;
import com.google.common.eventbus.Subscribe;
import org.age.hz.core.services.AbstractService;
import org.age.hz.core.services.topology.TopologyService;
import org.age.hz.core.services.worker.event.InitializeEvent;
import org.age.hz.core.services.worker.event.StartComputationEvent;
import org.age.hz.core.services.worker.state.WorkerState;
import org.jgrapht.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Map;
import java.util.Set;

@Component
public class WorkerServiceImpl extends AbstractService implements SmartLifecycle, WorkerService {

    private static final Logger log = LoggerFactory.getLogger(WorkerServiceImpl.class);

    private final TopologyService topologyService;

    private final int minimalNumberOfClients;

    @Inject
    public WorkerServiceImpl(@Value("${cluster.minimal.clients:3}") int minimalNumberOfClients, TopologyService topologyService) {
        this.minimalNumberOfClients = minimalNumberOfClients;
        this.topologyService = topologyService;
    }


    private Map<String, WorkerState> computationState;

    private WorkerState workerState = WorkerState.INIT;

    @PostConstruct
    public void init() {
        computationState = hazelcastInstance.getMap("worker/computationState");
        eventBus.register(this);
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        log.debug("Stop worker service with callback");
        stop();
        callback.run();
    }

    @Override
    public void start() {
        eventBus.post(new InitializeEvent());
        log.debug("Start worker service");
    }

    @Override
    public void stop() {
        log.debug("Stop worker service");
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public int getPhase() {
        return 10;
    }

    @Subscribe
    public void initialize(InitializeEvent initializeEvent) {
        if (workerState != WorkerState.INIT) {
            log.info("Already initialized");
            return;
        }

        switch (getComputationState()) {
            case INIT:
                if (topologyService.isLocalNodeMaster()) {
                    Integer topologySize = topologyService
                            .getTopologyGraph()
                            .map(Graph::vertexSet)
                            .map(Set::size)
                            .orElse(-1);

                    if (minimalNumberOfClients <= topologySize) {
                        // todo fire globally START_COMPUTATION
                        setGlobalComputationState(WorkerState.WORKING);
                        workerState = WorkerState.WORKING;
                        log.info("Starting computation");
                    } else {
                        log.info("Waiting for more nodes. Currently {} but needed at least {}", topologySize, minimalNumberOfClients);
                    }
                } else {
                    log.info("Cannot start computation - current node is not a master");
                }
                break;
            case WORKING:
                log.debug("Already working");
                eventBus.post(new StartComputationEvent());
                break;
            case FINISHED:
                log.info("Computation finished");
                System.exit(0);
        }
    }

    private WorkerState getComputationState() {
        return Optional
                .fromNullable(computationState
                        .get(WorkerConst.COMPUTATION_STATE))
                .or(WorkerState.INIT);
    }

    private void setGlobalComputationState(WorkerState state) {
        log.debug("Setting global computation state {}", state);
        computationState.put(WorkerConst.COMPUTATION_STATE, state);
    }

}