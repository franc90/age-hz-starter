package org.age.hz.core.services.worker.task;

import com.google.common.util.concurrent.ListenableScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

import static com.google.common.base.MoreObjects.toStringHelper;

public class NullTask implements Task {

    public static final NullTask INSTANCE = new NullTask();

    private static final Logger log = LoggerFactory.getLogger(NullTask.class);

    private NullTask() {
    }

    @Override
    public boolean isRunning() {
        log.warn("Checking 'running' status of the NULL task.");
        return false;
    }

    @Override
    public String className() {
        throw new UnsupportedOperationException("NULL task does not return values.");
    }

    @Override
    public AbstractApplicationContext springContext() {
        throw new UnsupportedOperationException("NULL task does not return values.");
    }

    @Override
    public ListenableScheduledFuture<?> future() {
        throw new UnsupportedOperationException("NULL task does not return values.");
    }

    @Override
    public Runnable runnable() {
        throw new UnsupportedOperationException("NULL task does not return values.");
    }

    @Override
    public void pause() {
        log.warn("Pausing up NULL task.");
    }

    @Override
    public void resume() {
        log.warn("Resuming NULL task.");
    }

    @Override
    public void stop() {
        log.warn("Stopping NULL task.");
    }

    @Override
    public void cleanUp() {
        log.warn("Cleaning up NULL task.");
    }

    @Override
    public void cancel() {
        log.warn("Cancelling NULL task.");
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof NullTask;
    }

    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }

}
