package org.age.hz.core.services;

import com.google.common.eventbus.EventBus;
import com.hazelcast.core.HazelcastInstance;
import org.age.hz.core.services.discovery.NodeId;

import javax.inject.Inject;

public abstract class AbstractService {

    @Inject
    protected HazelcastInstance hazelcastInstance;

    @Inject
    protected EventBus eventBus;

    @Inject
    protected NodeId nodeId;

}
