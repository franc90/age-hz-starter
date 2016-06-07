package org.age.hz.core.services.worker.task;

import com.google.common.util.concurrent.ListenableScheduledFuture;
import org.age.hz.core.compute.api.Pauseable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.concurrent.atomic.AtomicBoolean;

public class PauseableStartedTask extends StartedTask {

    private static final Logger log = LoggerFactory.getLogger(PauseableStartedTask.class);

    private final AtomicBoolean paused = new AtomicBoolean(false);

    PauseableStartedTask(final String className, final AbstractApplicationContext springContext,
                         final Pauseable runnable, final ListenableScheduledFuture<?> future) {
        super(className, springContext, runnable, future);
    }

    @Override
    public void pause() {
        if (paused.get()) {
            log.debug("The task has been already paused.");
            return;
        }
        if (!isRunning()) {
            log.warn("Cannot pause not running task.");
            return;
        }

        log.debug("Pausing the task {}.", runnable);
        ((Pauseable) runnable).pause();
        paused.set(true);
    }

    @Override
    public void resume() {
        if (!paused.get()) {
            log.debug("The task has not been paused.");
            return;
        }
        if (!isRunning()) {
            log.warn("Cannot resume finished task.");
            return;
        }

        log.debug("Resuming the task {}.", runnable);
        ((Pauseable) runnable).resume();
        paused.set(false);
    }


}
