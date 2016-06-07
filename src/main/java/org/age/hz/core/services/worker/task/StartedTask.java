package org.age.hz.core.services.worker.task;

import com.google.common.util.concurrent.ListenableScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.nonNull;

public class StartedTask implements Task {

    private static final Logger log = LoggerFactory.getLogger(StartedTask.class);

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final String className;

    private final AbstractApplicationContext springContext;

    protected final Runnable runnable;

    private final ListenableScheduledFuture<?> future;

    StartedTask(final String className, final AbstractApplicationContext springContext,
                final Runnable runnable, final ListenableScheduledFuture<?> future) {
        assert nonNull(className) && nonNull(springContext) && nonNull(runnable) && nonNull(future);

        this.className = className;
        this.springContext = springContext;
        this.runnable = runnable;
        this.future = future;
    }

    @Override
    public final boolean isRunning() {
        lock.readLock().lock();
        try {
            return !future.isDone();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public final String className() {
        return className;
    }

    @Override
    public final AbstractApplicationContext springContext() {
        return springContext;
    }

    /**
     * @return a future for the running task.
     * @throws IllegalStateException when task is not scheduled.
     */
    @Override
    public final ListenableScheduledFuture<?> future() {
        lock.readLock().lock();
        try {
            return future;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * @return the running task.
     * @throws IllegalStateException when task is not scheduled.
     */
    @Override
    public final Runnable runnable() {
        lock.readLock().lock();
        try {
            return runnable;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void pause() {
        log.debug("The task is not pauseable.");
    }

    @Override
    public void resume() {
        log.debug("The task is not pauseable.");
    }

    @Override
    public void stop() {
        if (!isRunning()) {
            log.warn("Task is already stopped.");
            return;
        }

        log.debug("Stopping task {}.", runnable);
        lock.writeLock().lock();
        try {
            final boolean canceled = future.cancel(true);
            if (!canceled) {
                log.warn("Could not cancel the task. Maybe it already stopped?");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void cleanUp() {
        checkState(!isRunning(), "Task is not stopped.");

        log.debug("Cleaning up after task.");
        springContext.destroy();
    }

    @Override
    public void cancel() {
        if (!isRunning()) {
            log.warn("Task is already stopped.");
            return;
        }

        log.debug("Stopping task {}.", runnable);
        lock.writeLock().lock();
        try {
            final boolean canceled = future.cancel(true);
            if (!canceled) {
                log.warn("Could not cancel the task. Maybe it already stopped?");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            return toStringHelper(this).add("classname", className).add("runnable", runnable).toString();
        } finally {
            lock.readLock().unlock();
        }
    }


}
