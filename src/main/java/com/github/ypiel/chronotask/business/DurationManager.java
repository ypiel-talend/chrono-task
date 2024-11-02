package com.github.ypiel.chronotask.business;

import com.github.ypiel.chronotask.model.Task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class DurationManager {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture;
    private final List<Task> tasks = new ArrayList<>();
    private final AtomicBoolean isPaused = new AtomicBoolean(false);

    private final AtomicLong lastTime = new AtomicLong(0);


    public DurationManager() {
    }

    public void addTasks(Task task) {
        this.tasks.add(task);
    }

    public void removeTasks(Task task) {
        this.tasks.remove(task);
    }

    public void start() {
        lastTime.set(System.currentTimeMillis());

        scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            if (!isPaused.get()) {
                LocalDate now = LocalDate.now();

                long current = System.currentTimeMillis();
                for (Task t : tasks) {
                    List<Task.DurationByDate> durationsByDate = t.getDurationsByDate();
                    Optional<Task.DurationByDate> durationOfTodayOpt = durationsByDate.stream().filter(d -> d.getDate().equals(now)).findAny();

                    Task.DurationByDate durationOfToday;
                    if (!durationOfTodayOpt.isPresent()) {
                        durationOfToday = new Task.DurationByDate();
                        durationsByDate.add(durationOfToday);
                    } else {
                        durationOfToday = durationOfTodayOpt.get();
                    }


                    durationOfToday.setDuration(durationOfToday.getDuration().plusMillis(current - lastTime.get()));
                }
                lastTime.set(current);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void pause() {
        isPaused.set(true);
    }

    public void resume() {
        lastTime.set(System.currentTimeMillis());
        isPaused.set(false);
    }

    public void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        scheduler.shutdown();
    }

}
