package org.age.hz.core.tasks;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.age.hz.core.services.communication.event.ReceiveEvent;
import org.age.hz.core.tasks.utils.ReceivedMessageProcessor;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

import static com.google.common.base.MoreObjects.toStringHelper;

@Named
public class MessageReceiver implements Task {

    private static final Logger log = LoggerFactory.getLogger(MessageReceiver.class);

    private final ReceivedMessageProcessor receivedMessageProcessor;

    private boolean running;

    @Inject
    public MessageReceiver(EventBus eventBus, ReceivedMessageProcessor receivedMessageProcessor) {
        this.receivedMessageProcessor = receivedMessageProcessor;
        eventBus.register(this);
    }

    @Subscribe
    public void receiveMessage(ReceiveEvent event) {
        if (!running) {
            return;
        }
        receivedMessageProcessor.process(event);
    }

    @Override
    public void run() {
        log.info("MessageReceiver computation.");

        while (true) {
            running = true;
            try {
                Thread.sleep(RandomUtils.nextLong(0, 100));
            } catch (InterruptedException e) {
                log.error("Sleep interrupted");
            }
        }
    }

    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }

}
