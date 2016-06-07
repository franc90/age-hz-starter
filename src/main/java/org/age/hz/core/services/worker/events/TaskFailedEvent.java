package org.age.hz.core.services.worker.events;

import com.google.common.base.MoreObjects;
import org.age.hz.core.services.worker.WorkerService;

import java.time.LocalDateTime;

public class TaskFailedEvent {

    private final Throwable cause;

    private final LocalDateTime timestamp = LocalDateTime.now();

    public TaskFailedEvent(Throwable cause) {
        this.cause = cause;
    }

    public String serviceName() {
        return WorkerService.class.getSimpleName();
    }

    public Throwable getCause() {
        return cause;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("cause", cause)
                .add("timestamp", timestamp)
                .toString();
    }
}
