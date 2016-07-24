package org.age.hz.core.services.worker;

import org.age.hz.core.services.worker.enums.WorkerState;

public interface WorkerService {

    WorkerState getState();

    void setState(WorkerState state);

    void internalStart();

    void configure();

    void startTask();

    void pauseTask();

    void cancelTask();

    void resumeTask();

    void taskFinished();

    void taskFailed();

    void cleanUpAfterTask();

    void terminate();
}
