package org.age.hz.core.services.worker;

import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;
import org.age.hz.core.services.AbstractService;
import org.age.hz.core.services.topology.TopologyService;
import org.age.hz.core.services.worker.computation.ComputationState;
import org.age.hz.core.services.worker.enums.ConfigurationKey;
import org.age.hz.core.services.worker.enums.WorkerEvent;
import org.age.hz.core.services.worker.enums.WorkerState;
import org.age.hz.core.services.worker.events.TaskStartedEvent;
import org.age.hz.core.services.worker.messages.WorkerMessage;
import org.age.hz.core.services.worker.task.NullTask;
import org.age.hz.core.services.worker.task.Task;
import org.age.hz.core.services.worker.task.TaskBuilder;
import org.age.hz.core.services.worker.task.TaskExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static com.google.common.util.concurrent.MoreExecutors.shutdownAndAwaitTermination;
import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newScheduledThreadPool;

@Component
public class WorkerServiceImpl extends AbstractService implements SmartLifecycle, WorkerService {

    private static final Logger log = LoggerFactory.getLogger(WorkerServiceImpl.class);

    private final ListeningScheduledExecutorService executorService = listeningDecorator(newScheduledThreadPool(5));

    private final MessageListener<WorkerMessage<Serializable>> workerTopicListener;

    private final TopologyService topologyService;

    private ITopic<WorkerMessage<Serializable>> workerTopic;

    private Map<ConfigurationKey, Object> workerConfigurationMap;

    private IMap<String, ComputationState> workerComputationStateMap;

    private WorkerState state = WorkerState.OFFLINE;

    private TaskBuilder taskBuilder;

    private Task currentTask = NullTask.INSTANCE;

    @Inject
    public WorkerServiceImpl(TopologyService topologyService, MessageListener<WorkerMessage<Serializable>> workerTopicListener) {
        this.topologyService = topologyService;
        this.workerTopicListener = workerTopicListener;
    }

    @PostConstruct
    public void init() {
        workerTopic = hazelcastInstance.getTopic("worker/channel");
        workerTopic.addMessageListener(workerTopicListener);
        workerConfigurationMap = hazelcastInstance.getReplicatedMap("worker/configuration");
        workerComputationStateMap = hazelcastInstance.getMap("worker/state");
        eventBus.register(this);
    }

    @Override
    public WorkerState getState() {
        return state;
    }

    @Override
    public void setState(WorkerState state) {
        this.state = state;
    }

    @Override
    public void internalStart() {
        log.debug("Worker service starting");

        setWorkerComputationState(ComputationState.NONE);
        state = WorkerState.RUNNING;

        if (globalComputationState() == ComputationState.CONFIGURED) {
            eventBus.post(WorkerEvent.CONFIGURE);
        }

        if (globalComputationState() == ComputationState.RUNNING) {
            eventBus.post(WorkerEvent.CONFIGURE);
            eventBus.post(WorkerEvent.START_EXECUTION);
        }

        log.info("Worker service started");
    }

    @Override
    public void configure() {
        state = WorkerState.CONFIGURED;

        if (isTaskPresent()) {
            log.warn("Task is already configured.");
            return;
        }

        WorkerConfiguration configuration = (WorkerConfiguration) workerConfigurationMap.get(ConfigurationKey.CONFIGURATION);
        TaskBuilder classTaskBuilder = configuration.taskBuilder();
//       TODO prepareContext(classTaskBuilder);
        taskBuilder = classTaskBuilder;
        //TODO node computation state = configured
        changeGlobalComputationStateIfMaster(ComputationState.CONFIGURED);
    }

    private boolean isTaskPresent() {
        return nonNull(taskBuilder) && !currentTask.equals(NullTask.INSTANCE);
    }

    private void changeGlobalComputationStateIfMaster(ComputationState configured) {
        if (configured == null) {
            log.error("Trying to set null global computation configuration");
            return;
        }

        if (topologyService.isLocalNodeMaster()) {
            workerConfigurationMap.put(ConfigurationKey.COMPUTATION_STATE, configured);
        }
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
        log.debug("Start worker service");
        eventBus.post(WorkerEvent.START);
    }

    @Override
    public void stop() {
        log.debug("Stop worker service");
        eventBus.post(WorkerEvent.TERMINATE);
    }

    @Override
    public boolean isRunning() {
        boolean notInitialState = state != WorkerState.OFFLINE;
        boolean notTerminated = state != WorkerState.TERMINATED && state != WorkerState.FAILED;
        return notInitialState && notTerminated;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void startTask() {
        while (environmentIsNotReady()) {
            try {
                log.warn("Trying to start computation when node is not ready.");
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for environment");
            }
        }

        log.debug("Starting task {}.", taskBuilder);

//        todo communicationFacilities.forEach(CommunicationFacility::start);
        currentTask = taskBuilder.buildAndSchedule(executorService, new TaskExecutionListener(eventBus));
        eventBus.post(new TaskStartedEvent());
        setWorkerComputationState(ComputationState.RUNNING);
        changeGlobalComputationStateIfMaster(ComputationState.RUNNING);
        state = WorkerState.EXECUTING;
    }

    @Override
    public void pauseTask() {
        log.debug("Pausing current task {}.", currentTask);
        state = WorkerState.PAUSED;
        currentTask.pause();
    }

    @Override
    public void resumeTask() {
        log.debug("Resuming current task {}.", currentTask);
        state = WorkerState.EXECUTING;
        currentTask.resume();
    }

    @Override
    public void cancelTask() {
        log.debug("Cancelling current task {}.", currentTask);
        state = WorkerState.COMPUTATION_CANCELED;
        currentTask.cancel();
    }

    @Override
    public void taskFinished() {
        setWorkerComputationState(ComputationState.FINISHED);
        state = WorkerState.FINISHED;
        final Collection<ComputationState> states = workerComputationStateMap.values(
                v -> v.getValue() != ComputationState.FINISHED);
        if (states.isEmpty()) {
            log.debug("All nodes finished computation.");
            changeGlobalComputationStateIfMaster(ComputationState.FINISHED);
        }
    }

    @Override
    public void taskFailed() {
        state = WorkerState.COMPUTATION_FAILED;
        changeGlobalComputationStateIfMaster(ComputationState.FAILED);
    }

    @Override
    public void cleanUpAfterTask() {
        log.debug("Cleaning up after task {}.", currentTask);
        currentTask.cleanUp();
        currentTask = NullTask.INSTANCE;
        setWorkerComputationState(ComputationState.NONE);
        changeGlobalComputationStateIfMaster(ComputationState.NONE);
        log.debug("Clean up finished.");
    }

    @Override
    public void terminate() {
        log.debug("Topology service stopping.");
        shutdownAndAwaitTermination(executorService, 10L, TimeUnit.SECONDS);
        log.info("Topology service stopped.");
    }

    private boolean environmentIsNotReady() {
        return !topologyService.hasTopology();
    }

    private void setWorkerComputationState(ComputationState computationState) {
        workerComputationStateMap.set(myId.getNodeId(), computationState);
    }

    private ComputationState globalComputationState() {
        return configurationValue(ConfigurationKey.COMPUTATION_STATE, ComputationState.class)
                .orElseGet(() -> ComputationState.NONE);
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> configurationValue(final ConfigurationKey key,
                                               final Class<T> klass) {
        return Optional.ofNullable((T) workerConfigurationMap.get(key));
    }
}