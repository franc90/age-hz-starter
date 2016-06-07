package org.age.hz.core.services.worker.task;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.FutureCallback;
import org.age.hz.core.services.worker.enums.WorkerEvent;
import org.age.hz.core.services.worker.events.TaskFailedEvent;
import org.age.hz.core.services.worker.events.TaskFinishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CancellationException;

@Named
public class TaskExecutionListener implements FutureCallback<Object> {

    private static final Logger log = LoggerFactory.getLogger(TaskExecutionListener.class);

    @Inject
    private EventBus eventBus;

    private Task currentTask;

    @Override
    public void onSuccess(Object result) {
        log.info("Task {} finished", currentTask);
        eventBus.post(new TaskFinishedEvent());
        eventBus.post(WorkerEvent.COMPUTATION_FINISHED);
    }

    @Override
    public void onFailure(Throwable t) {
        if (t instanceof CancellationException) {
            log.debug("Task {} was cancelled. Ignoring exception.", currentTask);
        } else {
            log.error("Task {} failed with error.", currentTask);
            eventBus.post(new TaskFailedEvent(t));
        }
        eventBus.post(WorkerEvent.COMPUTATION_FAILED);
    }

}
