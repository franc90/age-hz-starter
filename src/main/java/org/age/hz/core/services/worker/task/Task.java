package org.age.hz.core.services.worker.task;

import com.google.common.util.concurrent.ListenableScheduledFuture;
import org.springframework.context.support.AbstractApplicationContext;

public interface Task {

    boolean isRunning();

    String className();

    AbstractApplicationContext springContext();

    ListenableScheduledFuture<?> future();

    Runnable runnable();

    void pause();

    void resume();

    void stop();

    void cleanUp();

    void cancel();

}
