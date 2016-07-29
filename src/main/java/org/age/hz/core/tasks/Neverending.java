package org.age.hz.core.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.google.common.base.MoreObjects.toStringHelper;

@Named
public class Neverending implements Task {

    private static final Logger log = LoggerFactory.getLogger(Neverending.class);

    @Override
    public void run() {
        log.info("Neverending computation.");

        IntStream.range(0, 100).forEach(this::iterate);
    }

    private void iterate(int iterationNumber) {
        log.info("Iteration {}.", iterationNumber);
        try {
            TimeUnit.SECONDS.sleep(1L);
        } catch (final InterruptedException e) {
            log.debug("Interrupted.", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }
}
