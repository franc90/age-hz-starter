package org.age.hz.core.services.communication;

import com.google.common.eventbus.Subscribe;
import com.hazelcast.core.ITopic;
import org.age.hz.core.services.AbstractService;
import org.age.hz.core.services.communication.event.SendEvent;
import org.age.hz.core.services.communication.listener.CommunicationMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class CommunicationServiceImpl extends AbstractService implements SmartLifecycle, CommunicationService {

    private static final Logger log = LoggerFactory.getLogger(CommunicationServiceImpl.class);

    private final CommunicationMessageListener communicationMessageListener;

    private ITopic<SendEvent> communicationTopic;

    @Inject
    public CommunicationServiceImpl(CommunicationMessageListener communicationMessageListener) {
        this.communicationMessageListener = communicationMessageListener;
    }

    @PostConstruct
    public void init() {
        eventBus.register(this);
        communicationTopic = hazelcastInstance.getTopic("communication/topic");
        communicationTopic.addMessageListener(communicationMessageListener);
    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    @Override
    public void stop(Runnable callback) {
        log.debug("Stopping communication service");
        callback.run();
    }

    @Override
    public void start() {
        log.debug("Starting communication service");
    }

    @Override
    public void stop() {
        log.debug("Stopping communication service");
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public int getPhase() {
        return 50;
    }

    @Subscribe
    public void publishEvent(SendEvent event) {
        log.debug("Publishing: {}", event);
        communicationTopic.publish(event);
    }


}
