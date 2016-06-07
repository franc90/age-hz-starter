package org.age.hz.core.services.worker.messages;

import com.google.common.eventbus.EventBus;
import org.age.hz.core.services.worker.enums.WorkerEvent;

import java.io.Serializable;

public class CleanConfigurationWorkerMessage<T extends Serializable> extends WorkerMessage<T> {

    public CleanConfigurationWorkerMessage(T payload) {
        super(payload);
    }

    @Override
    public void accept(EventBus eventBus) {
        eventBus.post(WorkerEvent.CLEAN);
    }

}
