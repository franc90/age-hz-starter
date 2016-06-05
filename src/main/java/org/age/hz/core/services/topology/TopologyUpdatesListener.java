package org.age.hz.core.services.topology;

import com.google.common.eventbus.EventBus;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import org.age.hz.core.services.topology.messages.TopologyMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class TopologyUpdatesListener implements MessageListener<TopologyMessage> {

    private static final Logger log = LoggerFactory.getLogger(TopologyUpdatesListener.class);

    @Inject
    private EventBus eventBus;

    @PostConstruct
    public void init() {
        eventBus.register(this);
    }

    @Override
    public void onMessage(Message<TopologyMessage> message) {
        final TopologyMessage topologyMessage = message.getMessageObject();
        log.debug("Topology update event: {}", topologyMessage);
        topologyMessage.getEvent().ifPresent(eventBus::post);
    }

}
