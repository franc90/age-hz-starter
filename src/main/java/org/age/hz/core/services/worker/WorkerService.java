package org.age.hz.core.services.worker;

import org.age.hz.core.services.worker.enums.WorkerState;

public interface WorkerService {

    WorkerState getState();


    void internalStart();

    void configure();
}
