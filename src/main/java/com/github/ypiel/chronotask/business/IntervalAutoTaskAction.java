package com.github.ypiel.chronotask.business;

import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.Action;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.geometry.Pos;


public class IntervalAutoTaskAction implements AutoTaskAction {

    private Destination destination;
    private AtomicInteger counter = new AtomicInteger(0);

    public IntervalAutoTaskAction() {
    }

    @Override
    public void run() {
        if (this.destination.getSelectedMainTask() == null) {
            return;
        }

        int skipped = counter.incrementAndGet();
        if (skipped > 2) {
            destination.pause();
        }

        System.out.println("IntervalAutoTaskAction: " + this.destination.getSelectedMainTask().getViewId());
        String title = this.destination.getSelectedMainTask().getViewId() + ": " + this.destination.getSelectedMainTask().getShortDescription();
        title = (this.destination.isPaused() ? "ChronoTask is paused: " : "Continue: ") + title;

        Notifications.create()
                .title(title)
                .hideAfter(javafx.util.Duration.seconds(7))
                .position(Pos.BOTTOM_RIGHT)
                .action(new Action("Continue", event -> {
                            this.destination.resume();
                            counter.set(0);
                        }),
                        new Action("Pause", event -> {
                            this.destination.pause();
                            counter.set(0);
                        }),
                        new Action("Change", event -> {
                            this.destination.moveToFront();
                            counter.set(0);
                        }),
                        new Action("Unselect all", event -> {
                            this.destination.unselectAll();
                            counter.set(0);
                        }))
                .showConfirm();

    }

    @Override
    public Duration getInterval() {
        return Duration.ofMinutes(25);
        //return Duration.ofSeconds(5);
    }

    @Override
    public void setDestination(Destination destination) {
        this.destination = destination;
    }

}
