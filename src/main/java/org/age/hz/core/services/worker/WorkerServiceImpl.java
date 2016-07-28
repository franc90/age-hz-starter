package org.age.hz.core.services.worker;

import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.MapListener;
import org.age.hz.core.services.AbstractService;
import org.age.hz.core.services.topology.TopologyService;
import org.age.hz.core.services.worker.enums.WorkerConst;
import org.age.hz.core.services.worker.event.ExitEvent;
import org.age.hz.core.services.worker.event.InitializeEvent;
import org.age.hz.core.services.worker.event.StartComputationEvent;
import org.age.hz.core.services.worker.state.GlobalComputationState;
import org.age.hz.core.services.worker.state.WorkerState;
import org.age.hz.core.tasks.Task;
import org.age.hz.core.utils.Runnables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class WorkerServiceImpl extends AbstractService implements SmartLifecycle, WorkerService {

    private static final Logger log = LoggerFactory.getLogger(WorkerServiceImpl.class);

    private final MapListener computationStateListener;

    private final List<Task> tasks;

    private final TopologyService topologyService;

    private final FutureCallback<Object> taskExecutionListener;

    private final int minimalNumberOfClients;

    private IMap<String, GlobalComputationState> computationState;

    private final ListeningScheduledExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(5));

    private WorkerState workerState = WorkerState.INIT;

    @Inject
    public WorkerServiceImpl(@Value("${cluster.minimal.clients:3}") int minimalNumberOfClients,
                             MapListener computationStateListener,
                             List<Task> tasks,
                             TopologyService topologyService,
                             FutureCallback<Object> taskExecutionListener) {
        this.minimalNumberOfClients = minimalNumberOfClients;
        this.computationStateListener = computationStateListener;
        this.tasks = tasks;
        this.topologyService = topologyService;
        this.taskExecutionListener = taskExecutionListener;
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
        log.debug("Stop worker service");
        MoreExecutors.shutdownAndAwaitTermination(executorService, 10L, TimeUnit.SECONDS);
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

        int nodesInTopology = topologyService.getNodesInTopology();
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
    public void startComputation(StartComputationEvent startComputationEvent) {
        if (workerState == WorkerState.WORKING) {
            log.debug("Node already working");
            return;
        } else if (workerState == WorkerState.FINISHED) {
            log.debug("Computation already finished");
            return;
        }

        workerState = WorkerState.WORKING;
        startTask();
    }

    private void startTask() {
        log.debug("Start computation");

        final String taskName = "RandomlyBreaking";

        Task task = tasks
                .stream()
                .filter(t -> taskName.equals(t.getName()))
                .findAny()
                .orElse(null);

        if (task == null) {
            log.debug("Task {} not found. :(", taskName);
            return;
        }

        ListenableScheduledFuture<?> future = executorService.schedule(Runnables.withThreadName("COMPUTE", task), 0L, TimeUnit.SECONDS);
        Futures.addCallback(future, taskExecutionListener);
    }

    @Subscribe
    public void terminate(ExitEvent exitEvent) {
        log.debug("terminating");
        MoreExecutors.shutdownAndAwaitTermination(executorService, 10L, TimeUnit.SECONDS);
        System.exit(0);
    }

}