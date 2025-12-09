package com.github.ypiel.chronotask.model;

import com.github.ypiel.chronotask.ChronoTask;
import com.github.ypiel.chronotask.business.IntervalAutoTaskAction;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void testDefaults() {
        Task task = new Task();
        assertEquals(0, task.getOrder());
        assertEquals("", task.getJira());
        assertEquals("", task.getShortDescription());
        assertEquals("", task.getNotes());
        assertEquals(Category.Fix, task.getCategory());
        assertTrue(task.getTags().isEmpty());
        assertTrue(task.getDurationsByDate().isEmpty());
        assertEquals(IntervalAutoTaskAction.class, task.getAutoTaskAction());
    }

    @Test
    void testIsValid() {
        Task task = new Task();
        assertFalse(task.isValid(), "New task should be invalid (order 0, empty desc)");

        task.setOrder(1);
        assertFalse(task.isValid(), "Task with order but empty desc should be invalid");

        task.setShortDescription("Fix bug");
        assertTrue(task.isValid(), "Task with order and desc should be valid");

        task.setShortDescription("   ");
        assertFalse(task.isValid(), "Task with whitespace desc should be invalid");
    }

    @Test
    void testGetViewId() {
        Task task = new Task();
        task.setJira("PROJ-123");
        assertEquals("PROJ-123", task.getViewId());

        task.setJira("http://jira.server.com/browse/PROJ-456");
        assertEquals("PROJ-456", task.getViewId());

        task.setJira("https://github.com/ypiel/chrono-task/issues/1");
        assertEquals("1", task.getViewId());
    }

    @Test
    void testIsIdUrl() {
        Task task = new Task();
        task.setJira("PROJ-123");
        assertFalse(task.isIdUrl());

        task.setJira("http://jira.server.com");
        assertTrue(task.isIdUrl());

        task.setJira("https://jira.server.com");
        assertTrue(task.isIdUrl());
    }

    @Test
    void testIsDone() {
        Task task = new Task();
        assertFalse(task.isDone());

        task.getTags().add("WIP");
        assertFalse(task.isDone());

        task.getTags().add(ChronoTask.DONE_STATUS);
        assertTrue(task.isDone());
    }

    @Test
    void testCompareTo() {
        Task t1 = new Task();
        t1.setOrder(1);

        Task t2 = new Task();
        t2.setOrder(2);

        // Neither done
        assertTrue(t1.compareTo(t2) < 0);
        assertTrue(t2.compareTo(t1) > 0);
        assertEquals(0, t1.compareTo(t1));

        // t1 done
        t1.getTags().add(ChronoTask.DONE_STATUS);
        assertTrue(t1.compareTo(t2) < 0, "Done task should come before Undone task? Wait let's check logic");
        // Logic:
        // if (this.isDone() && !other.isDone()) return -1; => Done first? or Done
        // smaller?
        // Usually dependent on where it is shown.
        // Let's verify expectations based on code:
        // isDone && !other.isDone -> -1 (this < other)

        // t2 done, t1 done
        t2.getTags().add(ChronoTask.DONE_STATUS);
        // Both done -> fallback to order
        assertTrue(t1.compareTo(t2) < 0);

        // t1 not done, t2 done
        t1.getTags().remove(ChronoTask.DONE_STATUS);
        // !isDone && other.isDone -> 1 (this > other)
        assertTrue(t1.compareTo(t2) > 0);
    }

    @Test
    void testDurationByDate() {
        Task.DurationByDate dbd = new Task.DurationByDate();
        assertEquals(LocalDate.now(), dbd.getDate());
        assertEquals(Duration.ZERO, dbd.getDuration());
        assertEquals("", dbd.getNotes());

        Task.DurationByDate dbd2 = new Task.DurationByDate(LocalDate.of(2023, 1, 1), Duration.ofHours(1), "Work");
        assertEquals(LocalDate.of(2023, 1, 1), dbd2.getDate());
        assertEquals(Duration.ofHours(1), dbd2.getDuration());
        assertEquals("Work", dbd2.getNotes());
    }
}
