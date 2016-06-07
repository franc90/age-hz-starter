package org.age.hz.core.services.discovery;

import com.google.common.collect.ImmutableSet;
import com.hazelcast.core.IMap;
import com.hazelcast.core.LifecycleListener;
import com.hazelcast.map.listener.MapListener;
import org.age.hz.core.node.NodeId;
import org.age.hz.core.services.AbstractService;
import org.age.hz.core.services.discovery.listeners.HazelcastLifecycleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Named
public class DiscoveryServiceImpl extends AbstractService implements SmartLifecycle, DiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(DiscoveryServiceImpl.class);

    private final AtomicBoolean running = new AtomicBoolean(false);

    @Inject
    private LifecycleListener lifecycleListener;

    @Inject
    private MapListener neighboursListener;

    private IMap<String, NodeId> members;

    private String neighboursListenerId;

    @PostConstruct
    public void init() {
        members = hazelcastInstance.getMap("discovery/members");
        neighboursListenerId = members.addEntryListener(neighboursListener, true);
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void start() {
        log.debug("Starting discovery service");
        log.debug("Neighbours: {} ");
        running.set(true);
        hazelcastInstance.getLifecycleService().addLifecycleListener(lifecycleListener);
        members.set(myId.getNodeId(), myId);
        log.debug("Discovery service started");
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public void stop() {
        log.debug("Stopping discovery service");
        if (hazelcastInstance.getLifecycleService().isRunning()) {
            cleanUp();
        }
        running.set(false);
        log.debug("Discovery service stopped");
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE + 1;
    }


    @Override
    public Set<NodeId> getAllMembers() {
        return ImmutableSet.copyOf(members.values());
    }

    @Override
    public Optional<NodeId> getMember(NodeId id) {
        return Optional.ofNullable(members.get(id));
    }

    @Override
    public void cleanUp() {
        members.removeEntryListener(neighboursListenerId);
        log.debug("Deleting myself from members map");
        members.remove(myId.getNodeId());
    }
}