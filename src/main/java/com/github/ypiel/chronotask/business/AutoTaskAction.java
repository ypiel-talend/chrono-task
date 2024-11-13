package com.github.ypiel.chronotask.business;

import com.github.ypiel.chronotask.model.Task;

import java.time.Duration;

public interface AutoTaskAction extends Runnable {
    void run();

    Duration getInterval();

    void setTask(Task task);

}
