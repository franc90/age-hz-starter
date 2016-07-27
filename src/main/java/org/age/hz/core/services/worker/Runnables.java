package org.age.hz.core.services.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runnables {

    private static final Logger log = LoggerFactory.getLogger(Runnables.class);

    private Runnables() {
    }

    public static Runnable swallowingRunnable(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                log.error("Runnable threw an error.", t);
            }
        };
    }

    public static Runnable withThreadName(String name, Runnable runnable) {
        return () -> {
            final String oldName = Thread.currentThread().getName();
            try {
                Thread.currentThread().setName(name);
                runnable.run();
            } finally {
                Thread.currentThread().setName(oldName);
            }
        };
    }
}
