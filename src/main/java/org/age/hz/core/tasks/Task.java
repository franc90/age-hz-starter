package org.age.hz.core.tasks;

public interface Task extends Runnable {

    default String getName() {
        return getClass().getSimpleName();
    }

}
