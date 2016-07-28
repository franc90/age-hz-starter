package org.age.hz.core.services.worker.computation;

import com.google.common.eventbus.EventBus;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import org.age.hz.core.services.topology.TopologyService;
import org.age.hz.core.services.worker.event.InitializeEvent;
import org.age.hz.core.services.worker.state.GlobalComputationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class ComputationStateListener implements EntryAddedListener<String, GlobalComputationState>, EntryUpdatedListener<String, GlobalComputationState> {

    private static final Logger log = LoggerFactory.getLogger(ComputationStateListener.class);

    private final TopologyService topologyService;

    private final EventBus eventBus;

    @Inject
    public ComputationStateListener(TopologyService topologyService, EventBus eventBus) {
        this.topologyService = topologyService;
        this.eventBus = eventBus;
    }

    @Override
    public void entryAdded(EntryEvent<String, GlobalComputationState> event) {
        if (event.getValue() == GlobalComputationState.COMPUTING) {
            log.trace("Entry added {}", event);
            eventBus.post(new InitializeEvent());
        }
    }

    @Override
    public void entryUpdated(EntryEvent<String, GlobalComputationState> event) {
        if (event.getValue() == GlobalComputationState.COMPUTING) {
            log.trace("Entry updated {}", event);
            eventBus.post(new InitializeEvent());
        }
    }
}
