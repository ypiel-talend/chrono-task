package com.github.ypiel.chronotask.control;

import com.github.ypiel.chronotask.ChronoTask;
import com.github.ypiel.chronotask.model.Status;
import com.github.ypiel.chronotask.model.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;

public class TaskTableView extends TableView<Task> {

    public TaskTableView() {
        this(new ArrayList<>());
    }
    public TaskTableView(final List<Task> tasks) {
        this.setTasks(tasks);

        TableColumn<Task, Integer> orderColumn = new TableColumn<>("Order");
        orderColumn.setCellValueFactory(new PropertyValueFactory<>("order"));
        orderColumn.setOnEditCommit(event -> {
            Task task = event.getRowValue();
            task.setOrder(event.getNewValue());
        });

        TableColumn<Task, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setOnEditCommit(event -> {
            Task task = event.getRowValue();
            task.setId(event.getNewValue());
        });

        TableColumn<Task, String> shortDescriptionColumn = new TableColumn<>("Short Description");
        shortDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("shortDescription"));
        shortDescriptionColumn.setOnEditCommit(event -> {
            Task task = event.getRowValue();
            task.setShortDescription(event.getNewValue());
        });

        TableColumn<Task, String> notesColumn = new TableColumn<>("Notes");
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        notesColumn.setOnEditCommit(event -> {
            Task task = event.getRowValue();
            task.setNotes(event.getNewValue());
        });

        TableColumn<Task, Status> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setOnEditCommit(event -> {
            Task task = event.getRowValue();
            task.setStatus(event.getNewValue());
        });

        // Tags
        ObservableList<String> availableTags = FXCollections.observableArrayList(Arrays.asList(ChronoTask.mainTopics));
        TableColumn<Task, List<String>> tagsColumn = new TableColumn<>("Tags");
        tagsColumn.setCellFactory(column -> new TagsTableCell(availableTags));

        this.getColumns().addAll(orderColumn, idColumn, shortDescriptionColumn, statusColumn, tagsColumn, notesColumn);

        // Set cell factories to allow editing
        this.setEditable(true);
        orderColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        idColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        shortDescriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        notesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        statusColumn.setCellFactory(ComboBoxTableCell.forTableColumn(Status.values()));

        // Add a key event handler to add a new task when ENTER is pressed
        this.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    Task last = this.getItems().getLast();
                    if(!last.isValid()){
                        break;
                    }
                    this.getItems().add(new Task());
                    this.getSelectionModel().clearSelection();
                    break;
                default:
                    break;
            }
        });
    }

    public void setTasks(final List<Task> tasks) {
        this.setItems(FXCollections.observableArrayList(tasks.stream().filter(Task::isValid).toList()));
        this.getItems().add(new Task()); // Add empty line for task creation
    }
}
