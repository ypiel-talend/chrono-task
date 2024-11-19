package com.github.ypiel.chronotask.business;

import com.github.ypiel.chronotask.model.Task;

import java.time.Duration;

import dorkbox.notify.Notify;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class IntervalAutoTaskAction implements AutoTaskAction {

    private Task task;

    public IntervalAutoTaskAction() {
    }

    @Override
    public void run() {
        System.out.println("IntervalAutoTaskAction: " + this.task.getViewId());
        Notify.Companion.create()
                .title("IntervalAutoTaskAction")
                .text(this.task.getViewId())
                .hideAfter(10_000)
                .onClickAction(new Function1<Notify, Unit>() {
                    @Override
                    public Unit invoke(Notify notify) {
                        System.out.println("Clicked");
                        return Unit.INSTANCE;
                    }
                })
                .onCloseAction(new Function1<Notify, Unit>() {
                    @Override
                    public Unit invoke(Notify notify) {
                        System.out.println("Closed");
                        return Unit.INSTANCE;
                    }
                })
                .showInformation();
    }

    @Override
    public Duration getInterval() {
        return Duration.ofMinutes(25);
    }

    @Override
    public void setTask(Task task) {
        this.task = task;
    }
}
