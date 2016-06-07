package org.age.hz.core.services.worker.events;

import com.google.common.base.MoreObjects;

public class TaskFinishedEvent implements TaskEvent {

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }

}
