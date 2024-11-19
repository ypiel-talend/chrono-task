package com.github.ypiel.chronotask.business;

import com.github.ypiel.chronotask.model.Task;

import java.time.Duration;

import javafx.stage.Stage;

public interface AutoTaskAction extends Runnable {

    Duration getInterval();

    void setDestination(Destination destination);

    interface Destination {
        Task getSelectedMainTask();
        Task getSelectedTodo();
        void moveToFront();
        void unselectAll();
        void pause();
        void resume();
        boolean isPaused();
    }

}
