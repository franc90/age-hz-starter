package org.age.hz.core.services.worker.events;

import com.google.common.base.MoreObjects;

import java.time.LocalDateTime;

public class TaskStartedEvent implements TaskEvent {

    private final LocalDateTime timestamp = LocalDateTime.now();

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("timestamp", timestamp)
                .toString();
    }
}
