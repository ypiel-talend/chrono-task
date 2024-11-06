package com.github.ypiel.chronotask.control;

import com.github.ypiel.chronotask.ChronoTask;
import com.github.ypiel.chronotask.model.Status;
import com.github.ypiel.chronotask.model.Task;

import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.IntegerStringConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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


        TableColumn<Task, Void> openColumn = new TableColumn<>("Open");
        openColumn.setCellFactory(column -> new TableCell<Task, Void>() {
            private final Hyperlink openLink = new Hyperlink("open");

            {
                openLink.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    final String id = task.getId();
                    log.debug("Try to open link: {}", id);
                    if (id != null && id.startsWith("http")) {
                        try {
                            OpenLinkTask openLinkTask = new OpenLinkTask(id);
                            new Thread(openLinkTask).start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(openLink);
                }
            }
        });


        this.getColumns().addAll(orderColumn, idColumn, shortDescriptionColumn, statusColumn, tagsColumn, openColumn);

        // Set cell factories to allow editing
        this.setEditable(true);
        orderColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        idColumn.setCellFactory(column -> new TextFieldTableCell<Task, String>(new DefaultStringConverter()) {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // If Id is an URL dusplay only the last segment.
                    if (item.startsWith("http")) {
                        int i = item.lastIndexOf('/');
                        item = item.substring(i + 1);
                    }
                    setText(item);
                }
            }
        });
        shortDescriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        statusColumn.setCellFactory(ComboBoxTableCell.forTableColumn(Status.values()));

        // Add a key event handler to add a new task when ENTER is pressed
        this.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    Task last = this.getItems().getLast();
                    if (!last.isValid()) {
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

    private static class OpenLinkTask extends javafx.concurrent.Task<Void> {
        private final String id;

        public OpenLinkTask(String id) {
            this.id = id;
        }

        @Override
        protected Void call() throws Exception {
            try {
                log.debug("Open link: {}", id);
                Desktop.getDesktop().browse(new URI(id));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
