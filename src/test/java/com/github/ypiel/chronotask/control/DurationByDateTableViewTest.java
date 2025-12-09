package com.github.ypiel.chronotask.control;

import com.github.ypiel.chronotask.model.Task;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DurationByDateTableViewTest extends ControlTestBase {

    @Test
    void testSetDurationsByDate_Sorting() throws InterruptedException {
        runOnJfxThread(() -> {
            DurationByDateTableView tableView = new DurationByDateTableView();

            List<Task.DurationByDate> list = new ArrayList<>();
            Task.DurationByDate d1 = new Task.DurationByDate(LocalDate.now(), Duration.ZERO, "today");
            Task.DurationByDate d2 = new Task.DurationByDate(LocalDate.now().minusDays(1), Duration.ZERO, "yesterday");
            Task.DurationByDate d3 = new Task.DurationByDate(LocalDate.now().plusDays(1), Duration.ZERO, "tomorrow");

            list.add(d1);
            list.add(d2);
            list.add(d3);

            tableView.setDurationsByDate(list);

            ObservableList<Task.DurationByDate> items = tableView.getItems();
            assertEquals(3, items.size());

            // Expected sort: descending date (newest first)
            // o2.getDate().compareTo(o1.getDate())

            assertEquals(d3, items.get(0), "Tomorrow should be first");
            assertEquals(d1, items.get(1), "Today should be second");
            assertEquals(d2, items.get(2), "Yesterday should be last");
        });
    }

    @Test
    void testIsNoteEditing() throws InterruptedException {
        runOnJfxThread(() -> {
            DurationByDateTableView tableView = new DurationByDateTableView();
            assertFalse(tableView.isNoteEditing());
        });
    }
}
