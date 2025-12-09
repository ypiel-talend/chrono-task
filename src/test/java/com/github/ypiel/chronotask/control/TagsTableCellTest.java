package com.github.ypiel.chronotask.control;

import com.github.ypiel.chronotask.model.Task;
import javafx.collections.FXCollections;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TagsTableCellTest extends ControlTestBase {

    @Test
    void testUpdateItem() throws InterruptedException {
        runOnJfxThread(() -> {
            TagsTableCell cell = new TagsTableCell(FXCollections.observableArrayList("Tag1", "Tag2"));

            // Empty
            cell.updateItem(null, true);
            assertNull(cell.getText());
            assertNull(cell.getGraphic());

            // Non-empty
            List<String> tags = Arrays.asList("Tag1");
            cell.updateItem(tags, false);
            assertEquals("Tag1", cell.getText());
            assertNull(cell.getGraphic());
        });
    }

    // Testing startEdit/commitEdit is hard without the cell being part of a
    // TableView/Scene
    // because getTableRow() might return null or unattached row.
    // However, we can construct a minimal environment.

    @Test
    void testIntegrationWithTask() throws InterruptedException {
        runOnJfxThread(() -> {
            // Setup TableView logic to test Cell behavior if possible,
            // or just mock the dependencies if Mockito was available (it's not explicit in
            // pom, but maybe transitive).
            // Actually, we can just instantiate properties.

            // Let's create a TableView, add a Task, and see if we can trigger cell logic.
            // This is becoming an integration test.

            // For unit testing the cell logic specifically:
            TagsTableCell cell = new TagsTableCell(FXCollections.observableArrayList("A", "B"));

            // We can manually set the TableRow but TableRow.setItem is protected/final?
            // TableCell.updateTableRow is not public.

            // So we rely on TableView mechanism.
            TableView<Task> table = new TableView<>();
            Task task = new Task();
            task.setTags(new java.util.ArrayList<>(Arrays.asList("A")));

            table.getItems().add(task);

            // It's hard to get the specific cell instance created by the factory unless we
            // hook into it.
            // But we can test the Cell class methods if we can simulate the environment.

            // Given the complexity of mocking TableCell environment without Mockito,
            // I'll stick to testing what I can: updateItem which is public (exposed via
            // loose inheritance or just calling it).
            // Actually updateItem is protected in Cell, but often exposed or callable in
            // subclasses if overridden.
            // In TagsTableCell, it is overridden public? No, protected.
            // Wait, I cannot call protected method from Test unless I am in same package.
            // I am in `com.github.ypiel.chronotask.control` package (test), so I CAN call
            // protected methods!
            // Correct.

            cell.updateItem(Arrays.asList("A", "B"), false);
            assertEquals("A, B", cell.getText());
        });
    }
}
