package org.age.hz.core.services.communication.listener;

import com.google.common.eventbus.EventBus;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import org.age.hz.core.node.NodeId;
import org.age.hz.core.services.communication.event.ReceiveEvent;
import org.age.hz.core.services.communication.event.SendEvent;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class CommunicationMessageListener implements MessageListener<SendEvent> {

    private final EventBus eventBus;

    private final NodeId nodeId;

    @Inject
    public CommunicationMessageListener(EventBus eventBus, NodeId nodeId) {
        this.eventBus = eventBus;
        this.nodeId = nodeId;
    }

    @Override
    public void onMessage(Message<SendEvent> message) {
        SendEvent sendEvent = message.getMessageObject();
        if (!nodeId.getNodeId().equals(sendEvent.getRecipientId())) {
            return;
        }

        eventBus.post(new ReceiveEvent(sendEvent));
    }

}
