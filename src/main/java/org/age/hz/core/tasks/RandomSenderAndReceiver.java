package org.age.hz.core.tasks;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.age.hz.core.node.NodeId;
import org.age.hz.core.services.communication.event.ReceiveEvent;
import org.age.hz.core.services.topology.TopologyService;
import org.age.hz.core.tasks.utils.ReceivedMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class RandomSenderAndReceiver extends RandomRecipientMessageSender {

    private static final Logger log = LoggerFactory.getLogger(RandomSenderAndReceiver.class);

    private final ReceivedMessageProcessor receivedMessageProcessor;

    private boolean running;

    @Inject
    public RandomSenderAndReceiver(EventBus eventBus, TopologyService topologyService, NodeId myId, ReceivedMessageProcessor receivedMessageProcessor) {
        super(eventBus, topologyService, myId);
        eventBus.register(this);
        this.receivedMessageProcessor = receivedMessageProcessor;
    }

    @Override
    public void run() {
        log.info("RandomSenderAndReceiver computation.");
        running = true;
        super.run();
    }

    @Subscribe
    public void receiveMessage(ReceiveEvent event) {
        if (!running) {
            return;
        }

        receivedMessageProcessor.process(event);
    }

}
