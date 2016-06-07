package org.age.hz.core.services.worker.task;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import org.age.hz.core.compute.api.Pauseable;
import org.age.hz.core.services.worker.exceptions.FailedComputationSetupException;
import org.age.hz.core.util.Runnables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.nonNull;

public class TaskBuilder {

    private static final Logger log = LoggerFactory.getLogger(TaskBuilder.class);

    private final AtomicBoolean configured = new AtomicBoolean(false);

    private final String className;

    private final AbstractApplicationContext springContext;

    private TaskBuilder(final String className, final AbstractApplicationContext springContext) {
        assert nonNull(className) && nonNull(springContext);

        this.className = className;
        this.springContext = springContext;
    }

    public static TaskBuilder fromClass(final String className) {
        assert nonNull(className);

        try {
            log.debug("Setting up task from class {}.", className);

            log.debug("Creating internal Spring context.");
            final AnnotationConfigApplicationContext taskContext = new AnnotationConfigApplicationContext();

            // Configure task
            final BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(className);
            taskContext.registerBeanDefinition("runnable", builder.getBeanDefinition());

            log.debug("Task setup finished.");

            return new TaskBuilder(className, taskContext);
        } catch (final BeanCreationException e) {
            log.error("Cannot create the task from class.", e);
            throw new FailedComputationSetupException("Cannot create the task from class", e);
        }
    }

    public static TaskBuilder fromConfig(final String configPath) {
        assert nonNull(configPath);

        try {
            log.debug("Setting up task from config {}.", configPath);

            log.debug("Creating internal Spring context.");
            final FileSystemXmlApplicationContext taskContext = new FileSystemXmlApplicationContext(configPath);

            log.debug("Task setup finished.");

            return new TaskBuilder(taskContext.getType("runnable").getCanonicalName(), taskContext);
        } catch (final BeanCreationException e) {
            log.error("Cannot create the task from file.", e);
            throw new FailedComputationSetupException("Cannot create the task from file", e);
        }
    }

    public static TaskBuilder fromClasspathConfig(final String configPath) {
        assert nonNull(configPath);

        try {
            log.debug("Setting up task from config {}.", configPath);

            log.debug("Creating internal Spring context.");
            final ClassPathXmlApplicationContext taskContext = new ClassPathXmlApplicationContext(configPath);

            log.debug("Task setup finished.");

            return new TaskBuilder(taskContext.getType("runnable").getCanonicalName(), taskContext);
        } catch (final BeanCreationException e) {
            log.error("Cannot create the task from file.", e);
            throw new FailedComputationSetupException("Cannot create the task from file", e);
        }
    }

    public boolean isConfigured() {
        return configured.get();
    }

    public String className() {
        return className;
    }

    public AbstractApplicationContext springContext() {
        return springContext;
    }

    public void registerSingleton(final Object bean) {
        assert nonNull(bean);
        checkState(!isConfigured(), "Task is already configured.");

        log.debug("Registering {} as {} in application context.", bean.getClass().getSimpleName(), bean);
        springContext.getBeanFactory().registerSingleton(bean.getClass().getSimpleName(), bean);
    }

    public void finishConfiguration() {
        checkState(!isConfigured(), "Task is already configured.");

        try {
            assert !configured.get();
            springContext.refresh();
            configured.set(true);
        } catch (final BeansException e) {
            log.error("Cannot refresh the Spring context.", e);
            throw new FailedComputationSetupException("Cannot refresh the Spring context", e);
        }
    }

    public Task buildAndSchedule(final ListeningScheduledExecutorService executorService,
                                 final FutureCallback<Object> executionListener) {
        assert nonNull(executorService) && nonNull(executionListener);
        checkState(isConfigured(), "Task is not configured.");

        try {
            final Runnable runnable = (Runnable) springContext.getBean("runnable");
            log.info("Starting execution of {}.", runnable);
            final ListenableScheduledFuture<?> future = executorService.schedule(Runnables.withThreadName("COMPUTE", runnable),
                    0L, TimeUnit.SECONDS);
            Futures.addCallback(future, executionListener);
            if (runnable instanceof Pauseable) {
                return new PauseableStartedTask(className, springContext, (Pauseable) runnable, future);
            }
            return new StartedTask(className, springContext, runnable, future);
        } catch (final BeansException e) {
            log.error("Cannot get runnable from the context.", e);
            throw new FailedComputationSetupException("Cannot get runnable from the context.", e);
        }
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("classname", className).add("configured", configured.get()).toString();
    }

}
