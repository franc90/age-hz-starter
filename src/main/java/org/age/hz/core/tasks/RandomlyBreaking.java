package org.age.hz.core.tasks;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

@Named
public class RandomlyBreaking extends SimpleLongRunning {

    private static final Logger log = LoggerFactory.getLogger(RandomlyBreaking.class);

    @Override
    protected void additionalAction(int iter) {
        if (iter > 10) {
            double val = RandomUtils.nextDouble(0.0, 1.0);
            if (val < .4) {
                log.debug("{} > 0.4, exiting", val);
                throw new RuntimeException();
            }
        }
    }
}
