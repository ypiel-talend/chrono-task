package com.github.ypiel.chronotask.business;

import com.github.ypiel.chronotask.model.Task;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DurationManager {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture;
    private final List<Task> tasks = new ArrayList<>();
    private final AtomicBoolean isPaused = new AtomicBoolean(false);

    private final AtomicLong lastTime = new AtomicLong(0);

    private final List<DurationManagerListener> listeners = new ArrayList<>();


    public DurationManager() {
    }

    public void addListener(DurationManagerListener listener) {
        listeners.add(listener);
    }

    public List<Task> getTasks() {
        return Collections.unmodifiableList(this.tasks);
    }

    public void addTasks(Task task) {
        this.tasks.add(task);
        listeners.forEach(l -> l.onTaskDurationAddTask(this, task));
    }

    public void removeTasks(Task task) {
        this.tasks.remove(task);
        listeners.forEach(l -> l.onTaskDurationRemoveTask(this, task));
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


                    long millisToAdd = current - lastTime.get();
                    durationOfToday.setDuration(durationOfToday.getDuration().plusMillis(millisToAdd));
                    log.debug("Task {} + {}ms => duration: {}", t.getId(), millisToAdd,durationOfToday.getDuration());
                }
                lastTime.set(current);
            }
        }, 0, 1, TimeUnit.SECONDS);
        listeners.forEach(l -> l.onTaskDurationStart(this));
    }

    public void pause() {
        isPaused.set(true);
        listeners.forEach(l -> l.onTaskDurationPause(this));
    }

    public void resume() {
        lastTime.set(System.currentTimeMillis());
        isPaused.set(false);
        listeners.forEach(l -> l.onTaskDurationResume(this));
    }

    public void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        scheduler.shutdown();
        listeners.forEach(l -> l.onTaskDurationStop(this));
    }

    public String toString(){
        String action = this.isPaused.get() ? "[Pause(" : "[Chrono(";
        String sTasks = this.getTasks().stream().map(t -> t.getViewId()).collect(Collectors.joining(" / ", action, ")]"));
        return sTasks;
    }

    public interface DurationManagerListener {
        void onTaskDurationAddTask(DurationManager durationManager, Task task);
        void onTaskDurationRemoveTask(DurationManager durationManager, Task task);
        void onTaskDurationStart(DurationManager durationManager);
        void onTaskDurationStop(DurationManager durationManager);
        void onTaskDurationPause(DurationManager durationManager);
        void onTaskDurationResume(DurationManager durationManager);

    }

}
