package org.age.hz.core.services.worker;

import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;
import org.age.hz.core.services.AbstractService;
import org.age.hz.core.services.topology.TopologyService;
import org.age.hz.core.services.worker.computation.ComputationState;
import org.age.hz.core.services.worker.enums.ConfigurationKey;
import org.age.hz.core.services.worker.enums.WorkerEvent;
import org.age.hz.core.services.worker.enums.WorkerState;
import org.age.hz.core.services.worker.messages.WorkerMessage;
import org.age.hz.core.services.worker.task.NullTask;
import org.age.hz.core.services.worker.task.Task;
import org.age.hz.core.services.worker.task.TaskBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Component
public class WorkerServiceImpl extends AbstractService implements SmartLifecycle, WorkerService {

    private static final Logger log = LoggerFactory.getLogger(WorkerServiceImpl.class);

    @Inject
    private MessageListener<WorkerMessage<Serializable>> workerTopicListener;

    @Inject
    private TopologyService topologyService;

    private ITopic<WorkerMessage<Serializable>> workerTopic;

    private Map<ConfigurationKey, Object> workerConfigurationMap;

    private IMap<String, ComputationState> workerComputationStateMap;

    private WorkerState state = WorkerState.OFFLINE;

    private TaskBuilder taskBuilder;

    private Task currentTask = NullTask.INSTANCE;

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
    public void internalStart() {
        log.debug("Worker service starting");

        //TODO node computation state = none

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