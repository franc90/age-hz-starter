package org.age.hz.core.utils;

public interface Runnables {

    static Runnable withThreadName(String name, Runnable runnable) {
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
