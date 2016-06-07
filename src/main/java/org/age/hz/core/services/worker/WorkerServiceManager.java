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

    @Inject
    private WorkerService workerService;

    @Inject
    private EventBus eventBus;

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

}
