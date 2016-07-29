package org.age.hz.core.services.worker;

import com.google.common.eventbus.Subscribe;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.MapListener;
import org.age.hz.core.services.AbstractService;
import org.age.hz.core.services.topology.TopologyService;
import org.age.hz.core.services.worker.computation.ComputationManager;
import org.age.hz.core.services.worker.enums.WorkerConst;
import org.age.hz.core.services.worker.event.ExitEvent;
import org.age.hz.core.services.worker.event.InitializeEvent;
import org.age.hz.core.services.worker.event.StartComputationEvent;
import org.age.hz.core.services.worker.state.GlobalComputationState;
import org.age.hz.core.services.worker.state.WorkerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

@Named
public class WorkerServiceImpl extends AbstractService implements SmartLifecycle, WorkerService {

    private static final Logger log = LoggerFactory.getLogger(WorkerServiceImpl.class);

    private final MapListener computationStateListener;

    private final TopologyService topologyService;

    private final ComputationManager computationManager;

    private final int minimalNumberOfClients;

    private IMap<String, GlobalComputationState> computationState;

    private WorkerState workerState = WorkerState.INIT;

    @Inject
    public WorkerServiceImpl(@Value("${cluster.minimal.clients:3}") int minimalNumberOfClients,
                             MapListener computationStateListener,
                             TopologyService topologyService,
                             ComputationManager computationManager) {
        this.minimalNumberOfClients = minimalNumberOfClients;
        this.computationStateListener = computationStateListener;
        this.topologyService = topologyService;
        this.computationManager = computationManager;
    }

    @PostConstruct
    public void init() {
        computationState = hazelcastInstance.getMap("worker/computationState");
        computationState.addEntryListener(computationStateListener, true);
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
        computationManager.shutdown();
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
            log.debug("Worker already initialized");
            return;
        }

        GlobalComputationState computationState = getComputationState();
        if (computationState == GlobalComputationState.FINISHED) {
            log.debug("Cluster in FINISHED state");
            eventBus.post(new StartComputationEvent());
            return;
        } else if (computationState == GlobalComputationState.COMPUTING) {
            log.debug("Cluster already computing - join in!");
            eventBus.post(new StartComputationEvent());
            return;
        }

        if (!topologyService.isLocalNodeMaster()) {
            log.info("Cannot start computation - current node is not a master");
            return;
        }

        int nodesInTopology = topologyService.getNodesCount();
        if (minimalNumberOfClients > nodesInTopology) {
            log.info("Waiting for more nodes. [{} of {}]", nodesInTopology, minimalNumberOfClients);
            return;
        }

        log.info("Starting computation");
        setGlobalComputationState(GlobalComputationState.COMPUTING);
    }

    private GlobalComputationState getComputationState() {
        return Optional
                .ofNullable(computationState
                        .get(WorkerConst.COMPUTATION_STATE))
                .orElse(GlobalComputationState.INIT);
    }

    private void setGlobalComputationState(GlobalComputationState state) {
        if (!topologyService.isLocalNodeMaster()) {
            return;
        }
        log.debug("Setting global computation state {}", state);
        computationState.put(WorkerConst.COMPUTATION_STATE, state);
    }

    @Subscribe
    public void startComputation(StartComputationEvent startComputationEvent) throws InterruptedException {
        Thread.sleep(2000); // to be sure topology is configured

        if (workerState == WorkerState.WORKING) {
            log.debug("Node already working");
            return;
        } else if (workerState == WorkerState.FINISHED) {
            log.debug("Computation already finished");
            return;
        }

        workerState = WorkerState.WORKING;
        computationManager.startTask();
    }

    @Subscribe
    public void terminate(ExitEvent exitEvent) {
        log.debug("terminating");
        computationManager.shutdown();
        System.exit(0);
    }

}