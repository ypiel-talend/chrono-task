package com.github.ypiel.chronotask.control;

import com.github.ypiel.chronotask.ChronoTask;
import com.github.ypiel.chronotask.model.Task;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskTableViewTest extends ControlTestBase {

    @Test
    void testSetTasks_Sorting() throws InterruptedException {
        runOnJfxThread(() -> {
            TaskTableView table = new TaskTableView();

            Task t1 = new Task();
            t1.setOrder(1);
            t1.setShortDescription("First");

            Task t2 = new Task();
            t2.setOrder(2);
            t2.setShortDescription("Second");

            List<Task> list = new ArrayList<>();
            list.add(t1);
            list.add(t2);

            table.setTasks(list);

            // Logic: tasks.stream().filter(Task::isValid).sorted().toList().reversed();
            // Task compareTo:
            // if neither done: comparison by order (t1 < t2)
            // sorted: [t1, t2]
            // reversed: [t2, t1]

            var items = table.getItems();
            // items has an extra empty task at the end for creating new tasks.
            assertEquals(3, items.size(), "Should have 2 tasks + 1 new task placeholder");

            assertEquals(t2, items.get(0));
            assertEquals(t1, items.get(1));
            // The last one is the empty placeholder
            assertFalse(items.get(2).isValid());
        });
    }

    @Test
    void testFiltering() throws InterruptedException {
        runOnJfxThread(() -> {
            TaskTableView table = new TaskTableView();

            Task t1 = new Task();
            t1.setShortDescription("Important");
            t1.setOrder(1);

            Task t2 = new Task();
            t2.setShortDescription("Boring");
            t2.setOrder(2);

            List<Task> list = new ArrayList<>();
            list.add(t1);
            list.add(t2);

            table.setTasks(list);

            table.getFilterProperty().set("Important");

            // Filter logic: string contains filter
            // t1 has "Important"
            // t2 has "Boring"

            // Check items
            boolean foundT1 = false;
            boolean foundT2 = false;

            for (Task t : table.getItems()) {
                if (t == t1)
                    foundT1 = true;
                if (t == t2)
                    foundT2 = true;
            }

            assertTrue(foundT1, "Should contain matching task");
            assertFalse(foundT2, "Should not contain mismatched task");
        });
    }

    @Test
    void testHideClosed() throws InterruptedException {
        runOnJfxThread(() -> {
            TaskTableView table = new TaskTableView();

            Task t1 = new Task();
            t1.setShortDescription("Open");
            t1.setOrder(1);

            Task t2 = new Task();
            t2.setShortDescription("Closed");
            t2.setOrder(2);
            t2.getTags().add(ChronoTask.DONE_STATUS);

            List<Task> list = new ArrayList<>();
            list.add(t1);
            list.add(t2);

            table.setTasks(list);

            // Default hideClosed is false -> show all
            boolean foundClosed = table.getItems().stream().anyMatch(t -> t == t2);
            assertTrue(foundClosed, "Should show closed tasks by default");

            // Hide closed tasks
            table.setShowEnabledTasks(true);

            foundClosed = table.getItems().stream().anyMatch(t -> t == t2);
            assertFalse(foundClosed, "Should hide closed tasks when requested");

            boolean foundOpen = table.getItems().stream().anyMatch(t -> t == t1);
            assertTrue(foundOpen, "Should still show open tasks");
        });
    }
}
