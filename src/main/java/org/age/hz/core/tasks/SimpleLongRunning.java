package org.age.hz.core.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.google.common.base.MoreObjects.toStringHelper;

@Named
public class SimpleLongRunning implements Task {

    private static final Logger log = LoggerFactory.getLogger(SimpleLongRunning.class);

    @Override
    public void run() {
        log.info("This is the simplest possible example of a computation.");

        IntStream.range(0, 100).forEach(this::iterate);
    }

    private void iterate(int iterationNumber) {
        log.info("Iteration {}.", iterationNumber);

        additionalAction(iterationNumber);

        try {
            TimeUnit.SECONDS.sleep(1L);
        } catch (final InterruptedException e) {
            log.debug("Interrupted.", e);
            Thread.currentThread().interrupt();
        }
    }

    protected void additionalAction(int iter) {

    }

    @Override
    public String toString() {
        return toStringHelper(this).toString();
    }
}
