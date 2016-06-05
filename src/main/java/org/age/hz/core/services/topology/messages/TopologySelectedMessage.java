package org.age.hz.core.services.topology.messages;

import com.google.common.base.MoreObjects;
import org.age.hz.core.services.topology.TopologyEvent;

import java.util.Optional;

public class TopologySelectedMessage extends TopologyMessage {

    @Override
    public Optional<TopologyEvent> getEvent() {
        return Optional.of(TopologyEvent.TOPOLOGY_CONFIGURED);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }

}
