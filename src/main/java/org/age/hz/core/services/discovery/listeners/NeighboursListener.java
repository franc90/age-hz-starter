package org.age.hz.core.services.discovery.listeners;

import com.google.common.eventbus.EventBus;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import org.age.hz.core.node.NodeId;
import org.age.hz.core.services.discovery.events.MemberAddedEvent;
import org.age.hz.core.services.discovery.events.MemberRemovedEvent;
import org.age.hz.core.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class NeighboursListener implements EntryAddedListener<String, NodeId>,
        EntryRemovedListener<String, NodeId>,
        EntryEvictedListener<String, NodeId> {

    private static final Logger log = LoggerFactory.getLogger(NeighboursListener.class);

    private final EventBus eventBus;

    @Inject
    public NeighboursListener(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void entryAdded(EntryEvent<String, NodeId> event) {
        long timestamp = System.currentTimeMillis();
        log.warn("{},add,{},{}", TimeUtils.toString(timestamp),timestamp, event.getValue().getNodeId());

        log.debug("NeighboursListener entry added: {}", event);
        eventBus.post(new MemberAddedEvent(event));
    }

    @Override
    public void entryEvicted(EntryEvent<String, NodeId> event) {
        long timestamp = System.currentTimeMillis();
        log.warn("{},rmv,{},{}", TimeUtils.toString(timestamp), timestamp, event.getValue().getNodeId());

        log.debug("NeighboursListener entry evicted: {}", event);
        eventBus.post(new MemberRemovedEvent(event));
    }

    @Override
    public void entryRemoved(EntryEvent<String, NodeId> event) {
        long timestamp = System.currentTimeMillis();
        log.warn("{},rmv,{},{}", TimeUtils.toString(timestamp), timestamp, event.getKey());

        log.debug("NeighboursListener entry removed: {}", event);
        eventBus.post(new MemberRemovedEvent(event));
    }
}
