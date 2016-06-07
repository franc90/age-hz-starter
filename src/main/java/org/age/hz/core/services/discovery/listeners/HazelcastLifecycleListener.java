package org.age.hz.core.services.discovery.listeners;

import com.google.common.eventbus.EventBus;
import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;
import org.age.hz.core.services.discovery.DiscoveryService;
import org.age.hz.core.services.discovery.events.DiscoveryServiceStoppingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

import static com.hazelcast.core.LifecycleEvent.LifecycleState.SHUTTING_DOWN;

@Named
public class HazelcastLifecycleListener implements LifecycleListener {

    private static final Logger log = LoggerFactory.getLogger(HazelcastLifecycleListener.class);

    @Inject
    private DiscoveryService discoveryService;

    @Inject
    private EventBus eventBus;

    @Override
    public void stateChanged(LifecycleEvent event) {
        log.debug("Hazelcast state changed: {} ", event);
        if (event.getState() == SHUTTING_DOWN) {
            eventBus.post(new DiscoveryServiceStoppingEvent());
            discoveryService.cleanUp();
        }
    }

}
