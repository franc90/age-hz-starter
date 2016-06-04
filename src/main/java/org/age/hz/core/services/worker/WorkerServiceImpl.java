package org.age.hz.core.services.worker;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class WorkerServiceImpl implements SmartLifecycle, WorkerService {

    private static final Logger log = LoggerFactory.getLogger(WorkerServiceImpl.class);

    @Inject
    private HazelcastInstance hazelcastInstance;

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        log.debug("Stop worker service with callback");
        stop();
        callback.run();
    }

    @Override
    public void start() {
        log.debug("Start worker service");
    }

    @Override
    public void stop() {
        log.debug("Stop worker service");
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