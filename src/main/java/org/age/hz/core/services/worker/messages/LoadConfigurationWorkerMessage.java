package org.age.hz.core.services.worker.messages;

import com.google.common.eventbus.EventBus;
import org.age.hz.core.services.worker.enums.WorkerEvent;

import java.io.Serializable;

public class LoadConfigurationWorkerMessage<T extends Serializable> extends WorkerMessage<T> {

    public LoadConfigurationWorkerMessage() {
        super(null);
    }

    @Override
    public void accept(EventBus eventBus) {
        eventBus.post(WorkerEvent.CONFIGURE);
    }

}
