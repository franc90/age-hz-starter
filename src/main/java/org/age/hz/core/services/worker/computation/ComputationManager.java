package org.age.hz.core.services.worker.computation;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.age.hz.core.tasks.Task;
import org.age.hz.core.utils.Runnables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Named
public class ComputationManager {

    private static final Logger log = LoggerFactory.getLogger(ComputationManager.class);

    private final ListeningScheduledExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(5));

    private final List<Task> tasks;

    private final FutureCallback<Object> taskExecutionListener;

    private final String taskName;

    @Inject
    public ComputationManager(List<Task> tasks,
                              FutureCallback<Object> taskExecutionListener,
                              @Value("${computation.task:Simple}") String taskName) {
        this.tasks = tasks;
        this.taskExecutionListener = taskExecutionListener;
        this.taskName = taskName;
    }

    public void startTask() {
        log.debug("Start computation. Task: {}", taskName);

        Task task = tasks
                .stream()
                .filter(t -> taskName.equals(t.getName()))
                .findAny()
                .orElse(null);

        if (task == null) {
            log.debug("Task {} not found. :(", taskName);
            return;
        }

        ListenableScheduledFuture<?> future = executorService.schedule(Runnables.withThreadName("COMPUTE", task), 0L, TimeUnit.SECONDS);
        Futures.addCallback(future, taskExecutionListener);
    }


    public void shutdown() {
        MoreExecutors.shutdownAndAwaitTermination(executorService, 10L, TimeUnit.SECONDS);
    }
}
