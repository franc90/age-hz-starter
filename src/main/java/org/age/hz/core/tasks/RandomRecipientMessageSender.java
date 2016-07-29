package org.age.hz.core.tasks;

import com.google.common.eventbus.EventBus;
import org.age.hz.core.node.NodeId;
import org.age.hz.core.services.communication.event.SendEvent;
import org.age.hz.core.services.topology.TopologyService;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.MoreObjects.toStringHelper;

@Named
public class RandomRecipientMessageSender implements Task {

    private static final Logger log = LoggerFactory.getLogger(RandomRecipientMessageSender.class);

    private final EventBus eventBus;

    private final TopologyService topologyService;

    private final NodeId myId;

    @Inject
    public RandomRecipientMessageSender(EventBus eventBus, TopologyService topologyService, NodeId myId) {
        this.eventBus = eventBus;
        this.topologyService = topologyService;
        this.myId = myId;
    }

    @Override
    public void run() {
        log.info("RandomRecipientMessageSender computation.");

        while (true) {
            try {
                eventBus.post(buildMessage());
            } catch (IllegalStateException illegalState) {
                log.debug(illegalState.getMessage());
            } catch (Throwable throwable) {
                log.debug("Error while posting message", throwable);
            }

            try {
                Thread.sleep(RandomUtils.nextLong(1000, 4000));
            } catch (InterruptedException e) {
                log.error("Sleep interrupted");
            }
        }
    }

    private SendEvent buildMessage() {
        String recipientId = getRecipientId();
        long sendTimestamp = System.currentTimeMillis();

        return new SendEvent(myId.getNodeId(), recipientId, UUID.randomUUID(), sendTimestamp);
    }

    private String getRecipientId() {
        List<String> otherNodes = topologyService
                .getNodes()
                .stream()
                .filter(node -> !myId.getNodeId().equals(node))
                .collect(Collectors.toList());

        if (otherNodes.size() == 0) {
            return null;
        }

        int randomRecipientIdIndex = RandomUtils.nextInt(0, otherNodes.size());
        return otherNodes.get(randomRecipientIdIndex);
    }

    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }

}
