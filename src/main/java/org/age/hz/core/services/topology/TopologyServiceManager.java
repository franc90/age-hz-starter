package org.age.hz.core.services.topology;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.age.hz.core.services.discovery.events.MembershipChangedEvent;
import org.age.hz.core.services.topology.messages.TopologyEvent;
import org.age.hz.core.services.topology.state.TopologyState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;

import static org.age.hz.core.services.topology.state.TopologyState.*;

@Named
public class TopologyServiceManager {

    private static final Logger log = LoggerFactory.getLogger(TopologyServiceImpl.class);

    private static final Set<TopologyState> MEMBERSHIP_CHANGED_ELIGIBLE_STATED = ImmutableSet.of(MASTER, SLAVE, WITH_TOPOLOGY);

    private static final Set<TopologyState> TOPOLOGY_CHANGED_ELIGIBLE_STATES = ImmutableSet.of(MASTER, WITH_TOPOLOGY);

    private static final Set<TopologyState> TOPOLOGY_CONFIGURED_ELIGIBLE_STATES = ImmutableSet.of(MASTER, SLAVE, WITH_TOPOLOGY);

    private final TopologyService topologyService;

    private final EventBus eventBus;

    @Inject
    public TopologyServiceManager(TopologyService topologyService, EventBus eventBus) {
        this.topologyService = topologyService;
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void init() {
        log.info("starting TopologyServiceManager");
        eventBus.register(this);
    }

    @Subscribe
    public void processMembershipChangedEvent(MembershipChangedEvent membershipChanged) {
        log.debug("membership change: {}", membershipChanged);
        eventBus.post(TopologyEvent.MEMBERSHIP_CHANGED);
    }

    @Subscribe
    public void processTopologyEvent(TopologyEvent event) {
        switch (event) {
            case START:
                start();
                break;
            case MEMBERSHIP_CHANGED:
                membershipChanged();
                break;
            case STARTED:
                started();
                break;
            case TOPOLOGY_CHANGED:
                topologyChanged();
                break;
            case TOPOLOGY_CONFIGURED:
                topologyConfigured();
                break;
            case STOP:
                stop();
                break;
            case ERROR:
                error();
                break;
        }
    }

    private void start() {
        if (topologyService.getState() != OFFLINE) {
            log.debug("Topology service already started");
            return;
        }

        topologyService.setState(TopologyState.STARTING);
        topologyService.internalStart();
    }

    private void started() {
        if (topologyService.getState() != STARTING) {
            log.debug("Topology service not in {} state, cannot apply {} event", STARTING, TopologyEvent.STARTED);
        }

        topologyService.electMaster();
    }

    private void membershipChanged() {
        if (!MEMBERSHIP_CHANGED_ELIGIBLE_STATED.contains(topologyService.getState())) {
            log.debug("Current topology service state: {}. Cannot apply {} event", topologyService.getState(), TopologyEvent.MEMBERSHIP_CHANGED);
            return;
        }

        topologyService.electMaster();
    }

    private void topologyChanged() {
        if (!TOPOLOGY_CHANGED_ELIGIBLE_STATES.contains(topologyService.getState())) {
            log.debug("Current topology service state: {}. Cannot apply {} event", topologyService.getState(), TopologyEvent.TOPOLOGY_CHANGED);
            return;
        }

        topologyService.topologyChanged();
        topologyService.setState(WITH_TOPOLOGY);
    }

    private void topologyConfigured() {
        if (!TOPOLOGY_CONFIGURED_ELIGIBLE_STATES.contains(topologyService.getState())) {
            log.debug("Current topology service state: {}. Cannot apply {} event", topologyService.getState(), TopologyEvent.MEMBERSHIP_CHANGED);
            return;
        }

        topologyService.topologyConfiguredByMaster();
        topologyService.setState(WITH_TOPOLOGY);
    }

    private void error() {
        topologyService.handleError();
        topologyService.setState(FAILED);
    }

    private void stop() {
        topologyService.internalStop();
        topologyService.setState(TERMINATED);
    }


}
