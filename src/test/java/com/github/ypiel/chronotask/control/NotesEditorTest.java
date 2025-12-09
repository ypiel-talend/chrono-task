package com.github.ypiel.chronotask.control;

import com.github.ypiel.chronotask.model.Task;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotesEditorTest extends ControlTestBase {

    @Test
    void testSetTaskAndSave() throws InterruptedException {
        runOnJfxThread(() -> {
            NotesEditor editor = new NotesEditor();

            Task task = new Task();
            task.setShortDescription("My Task");
            task.setOrder(1);
            task.setNotes("Initial notes");

            editor.setTask(task);

            // Verify basic operation
            editor.save();
            assertEquals("Initial notes", task.getNotes());
        });
    }

    @Test
    void testRemoveTask() throws InterruptedException {
        runOnJfxThread(() -> {
            NotesEditor editor = new NotesEditor();
            Task task = new Task();
            task.setShortDescription("My Task");
            task.setOrder(1);

            editor.setTask(task);
            editor.removeTask();

            // Should be safe to call save
            try {
                editor.save();
            } catch (Exception e) {
                fail("Save after removeTask should not throw exception");
            }
        });
    }

    @Test
    void testEditNotesUpdatesTask() throws InterruptedException {
        runOnJfxThread(() -> {
            NotesEditor editor = new NotesEditor();
            Task task = new Task();
            task.setShortDescription("Editable Task");
            task.setOrder(1);
            task.setNotes("Original");

            editor.setTask(task);

            // Locate CodeArea to modify text
            // Structure: VBox -> [HBox, SplitPane]
            // SplitPane -> [VirtualizedScrollPane, WebView]
            // VirtualizedScrollPane -> CodeArea

            Node splitPaneNode = editor.getChildren().stream()
                    .filter(n -> n instanceof SplitPane)
                    .findFirst()
                    .orElse(null);

            assertNotNull(splitPaneNode, "Should find SplitPane");
            SplitPane splitPane = (SplitPane) splitPaneNode;

            Node scrollPaneNode = splitPane.getItems().stream()
                    .filter(n -> n instanceof VirtualizedScrollPane)
                    .findFirst()
                    .orElse(null);

            assertNotNull(scrollPaneNode, "Should find VirtualizedScrollPane");
            VirtualizedScrollPane<?> scrollPane = (VirtualizedScrollPane<?>) scrollPaneNode;

            assertTrue(scrollPane.getContent() instanceof CodeArea, "Content should be CodeArea");
            CodeArea codeArea = (CodeArea) scrollPane.getContent();

            // Modify text
            codeArea.replaceText("Modified Notes");

            // Trigger save
            editor.save();

            assertEquals("Modified Notes", task.getNotes());
        });
    }
}
