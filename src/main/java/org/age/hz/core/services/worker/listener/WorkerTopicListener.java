package org.age.hz.core.services.worker.listener;

import com.google.common.eventbus.EventBus;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import org.age.hz.core.node.NodeId;
import org.age.hz.core.services.worker.messages.WorkerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
public class WorkerTopicListener implements MessageListener<WorkerMessage<Serializable>> {

    private static final Logger log = LoggerFactory.getLogger(WorkerTopicListener.class);

    private final NodeId myId;

    private final EventBus eventBus;

    @Inject
    public WorkerTopicListener(NodeId myId, EventBus eventBus) {
        this.myId = myId;
        this.eventBus = eventBus;
    }

    @Override
    public void onMessage(Message<WorkerMessage<Serializable>> message) {
        WorkerMessage<Serializable> workerMessage = message.getMessageObject();
        if (workerMessage == null) {
            log.warn("Received null WorkerMessage");
            return;
        }

        log.debug("WorkerMessage received: {}", workerMessage);
        if (workerMessage.isNotRecipient(myId)) {
            log.debug("Message {} was not directed to me.", workerMessage);
            return;
        }

        process(workerMessage);
    }

    private void process(WorkerMessage<Serializable> workerMessage) {
        try {
            boolean eaten = false;
//            TODO
//            subscribedWorkerMessageListeners. for (listener -> {
//                log.debug("Notifying listener{}.", listener);
//                if (listener.onMessage(workerMessage)) {
//                    eaten = true;
//                    break;
//                }
//            }

            if (eaten) {
                return;
            }

            workerMessage.accept(eventBus);

        } catch (Throwable throwable) {
            log.info("T", throwable);
        }
    }

}
