package org.age.hz.core.tasks;

import org.age.hz.core.node.NodeId;
import org.age.hz.core.services.discovery.DiscoveryService;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class RandomlyBreaking extends SimpleLongRunning {

    private static final Logger log = LoggerFactory.getLogger(RandomlyBreaking.class);

    private final int initialIterations;

    private final double exceptionProbability;

    private final NodeId myId;

    private final DiscoveryService discoveryService;

    @Inject
    public RandomlyBreaking(@Value("${rand.task.initial.iterations:10}") int initialIterations,
                            @Value("${rand.task.exception.probability:0.3}") double exceptionProbability,
                            NodeId myId, DiscoveryService discoveryService) {
        this.initialIterations = initialIterations;
        this.exceptionProbability = exceptionProbability;
        this.myId = myId;
        this.discoveryService = discoveryService;
    }

    @Override
    protected void additionalAction(int iteration) {
        if (iteration > initialIterations) {
            double randomValue = RandomUtils.nextDouble(0.0, 1.0);
            if (randomValue < exceptionProbability) {
                log.debug("{} < {}, exiting", randomValue, exceptionProbability);

                discoveryService.stop();

                throw new RuntimeException();
            }
        }
    }
}
