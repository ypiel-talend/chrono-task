package com.github.ypiel.chronotask.control;

import com.github.ypiel.chronotask.model.Task;

import org.controlsfx.control.CheckComboBox;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;

public class TagsTableCell extends TableCell<Task, List<String>> {

    private CheckComboBox<String> checkComboBox;

    public TagsTableCell(ObservableList<String> availableTags) {
        this.checkComboBox = new CheckComboBox<>(availableTags);
        checkComboBox.getCheckModel().getCheckedItems().addListener((javafx.collections.ListChangeListener.Change<? extends String> c) -> {
            commitEdit(checkComboBox.getCheckModel().getCheckedItems());
        });
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if (!isEmpty()) {
            Task task = getTableRow().getItem();
            if (task != null) {
                List<String> currentTags = task.getTags();
                checkComboBox.getCheckModel().clearChecks();
                for (String tag : currentTags) {
                    checkComboBox.getCheckModel().check(tag);
                }
                setGraphic(checkComboBox);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        Task task = getTableRow().getItem();
        if (task != null) {
            setText(String.join(", ", task.getTags()));
        }
        setGraphic(null);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void commitEdit(List<String> tags) {
        super.commitEdit(tags);
        Task task = getTableRow().getItem();
        if (task != null) {
            List<String> selectedTags = new ArrayList<>(checkComboBox.getCheckModel().getCheckedItems());
            task.setTags(selectedTags);
            setText(String.join(", ", selectedTags));
            setGraphic(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
    }

    @Override
    protected void updateItem(List<String> tags, boolean empty) {
        super.updateItem(tags, empty);
    }
}
