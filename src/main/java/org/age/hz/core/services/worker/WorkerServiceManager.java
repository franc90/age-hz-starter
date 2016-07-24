package org.age.hz.core.services.worker;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.age.hz.core.services.worker.enums.WorkerEvent;
import org.age.hz.core.services.worker.enums.WorkerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class WorkerServiceManager {

    private static final Logger log = LoggerFactory.getLogger(WorkerServiceManager.class);

    private final WorkerService workerService;

    private final EventBus eventBus;

    @Inject
    public WorkerServiceManager(WorkerService workerService, EventBus eventBus) {
        this.workerService = workerService;
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void init() {
        log.info("Starting WorkerService");
        eventBus.register(this);
    }

    @Subscribe
    public void processWorkerEvent(WorkerEvent event) {
        log.debug("Received event: {}", event);
        switch (event) {
            case START:
                start();
                break;
            case CONFIGURE:
                configure();
                break;
            case START_EXECUTION:
                startTask();
                break;
            case PAUSE_EXECUTION:
                pauseTask();
                break;
            case RESUME_EXECUTION:
                resumeTask();
                break;
            case CANCEL_EXECUTION:
                cancelTask();
                break;
            case COMPUTATION_FINISHED:
                computationFinished();
                break;
            case COMPUTATION_FAILED:
                computationFailed();
                break;
            case CLEAN:
                cleanUpAfterTask();
                break;
            case TERMINATE:
                terminate();
                break;
            case ERROR:
                handleError();
                break;
            default:
                log.debug("Pass {}", event);
        }
    }

    private void start() {
        if (workerService.getState() != WorkerState.OFFLINE) {
            log.warn("WorkerService already started");
            return;
        }

        workerService.internalStart();
    }

    private void configure() {
        if (workerService.getState() != WorkerState.RUNNING) {
            log.warn("WorkerService not {} - wrong state: {}", WorkerState.RUNNING, workerService.getState());
            return;
        }

        workerService.configure();
    }

    private void startTask() {
        if (workerService.getState() != WorkerState.CONFIGURED) {
            log.warn("WorkerService not {} - wrong state: {}", WorkerState.CONFIGURED, workerService.getState());
            return;
        }

        workerService.startTask();
    }

    private void pauseTask() {
        if (workerService.getState() != WorkerState.EXECUTING) {
            log.warn("WorkerService not {} - wrong state: {}", WorkerState.EXECUTING, workerService.getState());
            return;
        }

        workerService.pauseTask();
    }

    private void resumeTask() {
        if (workerService.getState() != WorkerState.PAUSED) {
            log.warn("WorkerService not {} - wrong state: {}", WorkerState.PAUSED, workerService.getState());
            return;
        }

        workerService.resumeTask();
    }

    private void cancelTask() {
        if (workerService.getState() != WorkerState.CONFIGURED || workerService.getState() != WorkerState.EXECUTING || workerService.getState() != WorkerState.PAUSED) {
            log.warn("WorkerService not {} - wrong state: {}", WorkerState.CONFIGURED, workerService.getState());
            return;
        }

        workerService.cancelTask();
    }

    private void computationFinished() {
        if (workerService.getState() == WorkerState.EXECUTING) {
            workerService.taskFinished();
        }

        if (workerService.getState() == WorkerState.PAUSED) {
            workerService.setState(WorkerState.FINISHED);
        }


        log.warn("WorkerService not {}  or {} - wrong state: {}", WorkerState.EXECUTING, WorkerState.PAUSED, workerService.getState());
    }

    private void computationFailed() {
        if (workerService.getState() == WorkerState.EXECUTING) {
            workerService.taskFailed();
        }

        if (workerService.getState() == WorkerState.PAUSED) {
            workerService.setState(WorkerState.COMPUTATION_FAILED);
        }


        log.warn("WorkerService not {}  or {} - wrong state: {}", WorkerState.EXECUTING, WorkerState.PAUSED, workerService.getState());
    }

    private void cleanUpAfterTask() {
        if (workerService.getState() != WorkerState.FINISHED) {
            log.warn("WorkerService not {} - wrong state: {}", WorkerState.FINISHED, workerService.getState());
            return;
        }

        workerService.cleanUpAfterTask();
    }

    private void terminate() {
        workerService.terminate();
    }

    private void handleError() {
        // todo ERROR + fire ERROR
    }


}
