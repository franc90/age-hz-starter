package org.age.hz.core.services.worker.messages;

import com.google.common.eventbus.EventBus;
import org.age.hz.core.services.worker.enums.WorkerEvent;

import java.io.Serializable;

public class StartComputationWorkerMessage<T extends Serializable> extends WorkerMessage<T> {

    public StartComputationWorkerMessage() {
        super(null);
    }

    @Override
    public void accept(EventBus eventBus) {
        eventBus.post(WorkerEvent.START_EXECUTION);
    }
}
