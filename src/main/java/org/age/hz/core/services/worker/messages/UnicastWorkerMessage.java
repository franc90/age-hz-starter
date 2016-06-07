package org.age.hz.core.services.worker.messages;

import com.google.common.eventbus.EventBus;

import java.io.Serializable;
import java.util.Set;

public class UnicastWorkerMessage<T extends Serializable> extends WorkerMessage<T> {

    public UnicastWorkerMessage(Set<String> recipients, T payload, boolean broadcast) {
        super(recipients, payload, broadcast);
    }

    @Override
    public void accept(EventBus eventBus) {

    }
}
