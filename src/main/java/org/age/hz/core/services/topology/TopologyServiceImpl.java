package org.age.hz.core.services.topology;

import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class TopologyServiceImpl implements SmartLifecycle, TopologyService {

    private static final Logger log = LoggerFactory.getLogger(TopologyServiceImpl.class);

    @Inject
    private HazelcastInstance hazelcastInstance;

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        log.debug("Stop topology service with callback");
        stop();
        callback.run();
    }

    @Override
    public void start() {
        log.debug("Start topology service");
    }

    @Override
    public void stop() {
        log.debug("Stop topology service");
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE;
    }

}