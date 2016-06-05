package org.age.hz.core.services.topology.messages;

import org.age.hz.core.services.topology.TopologyEvent;

import java.io.Serializable;
import java.util.Optional;

public abstract class TopologyMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    public abstract Optional<TopologyEvent> getEvent();

}
