package org.age.hz.core.services.worker;

import org.age.hz.core.services.worker.task.TaskBuilder;

import java.io.Serializable;

public interface WorkerConfiguration extends Serializable {

    TaskBuilder taskBuilder();

}
