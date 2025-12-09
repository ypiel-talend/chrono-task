package com.github.ypiel.chronotask.business;

import com.github.ypiel.chronotask.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class DurationManagerTest {

    private DurationManager manager;
    private Task task;

    @BeforeEach
    void setUp() {
        manager = new DurationManager();
        task = new Task();
        task.setJira("TEST-1");
        task.setShortDescription("Test Task");
        task.setDurationsByDate(new java.util.ArrayList<>());
    }

    @AfterEach
    void tearDown() {
        manager.stop();
    }

    @Test
    void testAddRemoveTask() {
        assertFalse(manager.getTask().isPresent());

        manager.addTasks(task);
        assertTrue(manager.getTask().isPresent());
        assertEquals(task, manager.getTask().get());

        manager.removeTasks(task);
        assertFalse(manager.getTask().isPresent());
    }

    @Test
    void testListeners() {
        AtomicBoolean startCalled = new AtomicBoolean(false);
        AtomicBoolean stopCalled = new AtomicBoolean(false);
        AtomicBoolean pauseCalled = new AtomicBoolean(false);
        AtomicBoolean resumeCalled = new AtomicBoolean(false);

        manager.addListener(new DurationManager.DurationManagerListener() {
            @Override
            public void onTaskDurationAddTask(DurationManager durationManager, Task task) {
            }

            @Override
            public void onTaskDurationRemoveTask(DurationManager durationManager, Task task) {
            }

            @Override
            public void onTaskDurationStart(DurationManager durationManager) {
                startCalled.set(true);
            }

            @Override
            public void onTaskDurationStop(DurationManager durationManager) {
                stopCalled.set(true);
            }

            @Override
            public void onTaskDurationPause(DurationManager durationManager) {
                pauseCalled.set(true);
            }

            @Override
            public void onTaskDurationResume(DurationManager durationManager) {
                resumeCalled.set(true);
            }
        });

        manager.start();
        assertTrue(startCalled.get());

        manager.pause();
        assertTrue(pauseCalled.get());

        manager.resume();
        assertTrue(resumeCalled.get());

        manager.stop();
        assertTrue(stopCalled.get());
    }

    @Test
    void testDurationAccumulation() throws InterruptedException {
        manager.addTasks(task);
        manager.start();

        // Wait for at least 1 second + buffer
        Thread.sleep(2100); // 2 seconds to be safe

        manager.stop();

        assertFalse(task.getDurationsByDate().isEmpty());
        Task.DurationByDate dbd = task.getDurationsByDate().get(0);
        assertEquals(LocalDate.now(), dbd.getDate());
        assertTrue(dbd.getDuration().toMillis() > 0);
    }

    @Test
    void testToString() {
        assertTrue(manager.toString().contains("No task selected"));
        manager.addTasks(task);
        assertTrue(manager.toString().contains("TEST-1"));
        manager.pause();
        assertTrue(manager.toString().contains("Pause"));
    }
}
